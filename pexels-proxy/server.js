/**
 * Pexels Image Proxy Server
 *
 * A lightweight Node.js proxy that forwards search requests to the Pexels API.
 * The Pexels API key is stored as an environment variable and never exposed
 * to the client.
 *
 * Deploy twice:
 *   1. Koyeb  (primary) — configure external cron to hit /health every 45 min
 *   2. Render (backup)   — no keep-alive, allow cold starts
 *
 * Endpoints:
 *   GET /health              → { status: "ok" }
 *   GET /api/search?query=…  → { imageUrl, photographer, alt } | { error }
 *
 * Env:
 *   PEXELS_API_KEY  — required, Pexels API key
 *   PORT            — optional, defaults to 3000 (Koyeb/Render inject this)
 */

const http = require('http');
const https = require('https');

const PORT = process.env.PORT || 3000;
const PEXELS_API_KEY = process.env.PEXELS_API_KEY || '';
const PEXELS_HOST = 'api.pexels.com';
const CACHE_TTL_MS = 1000 * 60 * 60 * 2; // 2 hours

// --- Simple in-memory cache (per query string) ---
const cache = new Map();

function sendJson(res, status, body) {
  const json = JSON.stringify(body);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Cache-Control': 'public, max-age=7200',
  });
  res.end(json);
}

function fetchGitHubReleases(owner, repo, perPage) {
  return new Promise((resolve, reject) => {
    const path = `/repos/${owner}/${repo}/releases?per_page=${perPage}`;

    const options = {
      hostname: 'api.github.com',
      port: 443,
      path: path,
      method: 'GET',
      headers: {
        'Accept': 'application/vnd.github.v3+json',
        'User-Agent': 'ClearFlow-Proxy',
      },
      timeout: 10000,
    };

    const req = https.request(options, (resp) => {
      let data = '';
      resp.on('data', (chunk) => { data += chunk; });
      resp.on('end', () => {
        if (resp.statusCode !== 200) {
          reject(new Error(`GitHub API returned ${resp.statusCode}`));
          return;
        }
        try {
          const json = JSON.parse(data);
          resolve(json);
        } catch (e) {
          reject(new Error('Failed to parse GitHub response'));
        }
      });
    });

    req.on('error', reject);
    req.on('timeout', () => {
      req.destroy();
      reject(new Error('GitHub request timeout'));
    });

    req.end();
  });
}

function fetchPexels(query, orientation) {
  return new Promise((resolve, reject) => {
    if (!PEXELS_API_KEY) {
      reject(new Error('PEXELS_API_KEY not configured'));
      return;
    }

    const params = new URLSearchParams({
      query: query,
      orientation: orientation || 'landscape',
      per_page: '1',
      page: '1',
    });

    const path = `/v1/search?${params.toString()}`;

    const options = {
      hostname: PEXELS_HOST,
      port: 443,
      path: path,
      method: 'GET',
      headers: {
        'Authorization': PEXELS_API_KEY,
        'Accept': 'application/json',
      },
      timeout: 10000,
    };

    const req = https.request(options, (resp) => {
      let data = '';
      resp.on('data', (chunk) => { data += chunk; });
      resp.on('end', () => {
        if (resp.statusCode !== 200) {
          reject(new Error(`Pexels API returned ${resp.statusCode}`));
          return;
        }
        try {
          const json = JSON.parse(data);
          resolve(json);
        } catch (e) {
          reject(new Error('Failed to parse Pexels response'));
        }
      });
    });

    req.on('error', reject);
    req.on('timeout', () => {
      req.destroy();
      reject(new Error('Pexels request timeout'));
    });

    req.end();
  });
}

const server = http.createServer(async (req, res) => {
  // Handle preflight
  if (req.method === 'OPTIONS') {
    sendJson(res, 204, {});
    return;
  }

  const url = new URL(req.url, `http://localhost:${PORT}`);

  // --- Health check endpoint ---
  if (url.pathname === '/health') {
    sendJson(res, 200, {
      status: 'ok',
      timestamp: Date.now(),
      service: 'cleardu-pexels-proxy',
    });
    return;
  }

  // --- Root endpoint ---
  if (url.pathname === '/' || url.pathname === '/api') {
    sendJson(res, 200, {
      service: 'cleardu-pexels-proxy',
      endpoints: ['/health', '/api/search?query=…', '/api/github/releases?owner=…&repo=…'],
    });
    return;
  }

  // --- Search endpoint ---
  if (url.pathname === '/api/search') {
    const query = url.searchParams.get('query');
    const orientation = url.searchParams.get('orientation') || 'landscape';

    if (!query) {
      sendJson(res, 400, { error: 'Missing "query" parameter' });
      return;
    }

    const cacheKey = `${query}_${orientation}`;
    const cached = cache.get(cacheKey);
    if (cached && (Date.now() - cached.timestamp) < CACHE_TTL_MS) {
      sendJson(res, 200, cached.data);
      return;
    }

    try {
      const pexelsData = await fetchPexels(query, orientation);

      if (!pexelsData.photos || pexelsData.photos.length === 0) {
        sendJson(res, 404, { error: 'No photos found', imageUrl: null });
        return;
      }

      const photo = pexelsData.photos[0];
      const result = {
        imageUrl: photo.src?.original || photo.src?.large2x || photo.src?.large,
        photographer: photo.photographer || '',
        photographerUrl: photo.photographer_url || '',
        alt: photo.alt || query,
        avgColor: photo.avg_color || '',
        width: photo.width || 0,
        height: photo.height || 0,
      };

      // Cache the result
      cache.set(cacheKey, { data: result, timestamp: Date.now() });

      sendJson(res, 200, result);
    } catch (err) {
      console.error('[ERROR] Pexels fetch failed:', err.message);
      sendJson(res, 502, { error: err.message, imageUrl: null });
    }
    return;
  }

  // --- GitHub Releases endpoint ---
  if (url.pathname === '/api/github/releases') {
    const owner = url.searchParams.get('owner') || 'Jakeopsjj';
    const repo = url.searchParams.get('repo') || 'ClearFlow';
    const perPage = url.searchParams.get('per_page') || '30';

    const cacheKey = `gh_releases_${owner}_${repo}`;
    const cached = cache.get(cacheKey);
    if (cached && (Date.now() - cached.timestamp) < CACHE_TTL_MS) {
      sendJson(res, 200, cached.data);
      return;
    }

    try {
      const ghData = await fetchGitHubReleases(owner, repo, perPage);
      cache.set(cacheKey, { data: ghData, timestamp: Date.now() });
      sendJson(res, 200, ghData);
    } catch (err) {
      console.error('[ERROR] GitHub fetch failed:', err.message);
      sendJson(res, 502, { error: err.message });
    }
    return;
  }

  // --- 404 ---
  sendJson(res, 404, { error: 'Not found' });
});

server.listen(PORT, () => {
  console.log(`[cleardu-pexels-proxy] listening on port ${PORT}`);
  console.log(`[cleardu-pexels-proxy] PEXELS_API_KEY configured: ${PEXELS_API_KEY ? 'yes' : 'NO'}`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('[cleardu-pexels-proxy] SIGTERM received, shutting down...');
  server.close(() => process.exit(0));
});

process.on('SIGINT', () => {
  console.log('[cleardu-pexels-proxy] SIGINT received, shutting down...');
  server.close(() => process.exit(0));
});
