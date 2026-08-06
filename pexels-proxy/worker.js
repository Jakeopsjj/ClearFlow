/**
 * Cloudflare Workers — Pexels Image Proxy
 *
 * Wraps the Pexels API so the API key never reaches the Android client.
 * Uses Workers Cache API for 2-hour edge caching (reduces Pexels calls).
 *
 * Endpoints:
 *   GET /health              → { status: "ok" }
 *   GET /api/search?query=…  → { imageUrl, photographer, alt } | { error }
 *
 * Secret (set via `wrangler secret put PEXELS_API_KEY`):
 *   PEXELS_API_KEY  — required, Pexels API key
 */

const PEXELS_API_BASE = 'https://api.pexels.com/v1/search';
const CACHE_TTL_SECONDS = 60 * 60 * 2; // 2 hours

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type',
};

function jsonResponse(status, body, extraHeaders = {}) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
      'Cache-Control': 'public, max-age=7200',
      ...CORS_HEADERS,
      ...extraHeaders,
    },
  });
}

async function fetchPexels(query, orientation) {
  const apiKey = globalThis.PEXELS_API_KEY;
  if (!apiKey) {
    throw new Error('PEXELS_API_KEY not configured');
  }

  const params = new URLSearchParams({
    query,
    orientation: orientation || 'landscape',
    per_page: '1',
    page: '1',
  });

  const resp = await fetch(`${PEXELS_API_BASE}?${params.toString()}`, {
    method: 'GET',
    headers: {
      Authorization: apiKey,
      Accept: 'application/json',
    },
  });

  if (!resp.ok) {
    throw new Error(`Pexels API returned ${resp.status}`);
  }
  return resp.json();
}

async function handleSearch(request, url) {
  const query = url.searchParams.get('query');
  const orientation = url.searchParams.get('orientation') || 'landscape';

  if (!query) {
    return jsonResponse(400, { error: 'Missing "query" parameter' });
  }

  // --- Edge cache via Cache API ---
  const cacheKey = new Request(
    `https://cache.internal/api/search?query=${encodeURIComponent(query)}&orientation=${encodeURIComponent(orientation)}`,
    { method: 'GET' }
  );
  const cache = caches.default;
  const cached = await cache.match(cacheKey);
  if (cached) {
    return cached;
  }

  try {
    const pexelsData = await fetchPexels(query, orientation);

    if (!pexelsData.photos || pexelsData.photos.length === 0) {
      return jsonResponse(404, { error: 'No photos found', imageUrl: null });
    }

    const photo = pexelsData.photos[0];
    const result = {
      imageUrl: photo.src?.large2x || photo.src?.large || photo.src?.original,
      photographer: photo.photographer || '',
      photographerUrl: photo.photographer_url || '',
      alt: photo.alt || query,
      avgColor: photo.avg_color || '',
      width: photo.width || 0,
      height: photo.height || 0,
    };

    const response = jsonResponse(200, result);
    // Store in edge cache with TTL
    const cachedResponse = new Response(response.body, response);
    cachedResponse.headers.set('Cache-Control', `public, max-age=${CACHE_TTL_SECONDS}`);
    // Fire-and-forget cache put
    ctx().waitUntil(cache.put(cacheKey, cachedResponse));

    return response;
  } catch (err) {
    return jsonResponse(502, { error: err.message, imageUrl: null });
  }
}

// Helper to safely get ctx (ExecutionContext) — Workers pass it as 2nd arg
function ctx() {
  return globalThis.__CF_CTX || { waitUntil: (p) => p.catch(() => {}) };
}

export default {
  async fetch(request, env, executionContext) {
    // Expose ctx for helpers
    globalThis.__CF_CTX = executionContext;
    globalThis.PEXELS_API_KEY = env && env.PEXELS_API_KEY;

    // Handle preflight
    if (request.method === 'OPTIONS') {
      return new Response(null, { status: 204, headers: CORS_HEADERS });
    }

    let url;
    try {
      url = new URL(request.url);
    } catch (e) {
      return jsonResponse(400, { error: 'Invalid URL' });
    }

    // Health check
    if (url.pathname === '/health') {
      return jsonResponse(200, {
        status: 'ok',
        timestamp: Date.now(),
        service: 'cleardu-pexels-proxy',
      });
    }

    // Root / info
    if (url.pathname === '/' || url.pathname === '/api') {
      return jsonResponse(200, {
        service: 'cleardu-pexels-proxy',
        endpoints: ['/health', '/api/search?query=…'],
      });
    }

    // Search
    if (url.pathname === '/api/search') {
      return handleSearch(request, url);
    }

    return jsonResponse(404, { error: 'Not found' });
  },
};
