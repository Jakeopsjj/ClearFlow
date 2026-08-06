/**
 * Vercel Serverless Function: Pexels Image Search Proxy
 *
 * GET /api/search?query=sunny+landscape&orientation=landscape
 *
 * Env: PEXELS_API_KEY (set via Vercel dashboard or CLI)
 */

const https = require('https');

// Simple in-memory cache (per warm instance)
const cache = new Map();
const CACHE_TTL_MS = 1000 * 60 * 60 * 2; // 2 hours

function fetchPexels(query, orientation) {
  return new Promise((resolve, reject) => {
    const PEXELS_API_KEY = process.env.PEXELS_API_KEY || '';
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

    const options = {
      hostname: 'api.pexels.com',
      port: 443,
      path: `/v1/search?${params.toString()}`,
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
          resolve(JSON.parse(data));
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

module.exports = async (req, res) => {
  // CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  res.setHeader('Cache-Control', 'public, max-age=7200');

  if (req.method === 'OPTIONS') {
    return res.status(204).end();
  }

  const url = new URL(req.url, `http://${req.headers.host}`);
  const query = url.searchParams.get('query');
  const orientation = url.searchParams.get('orientation') || 'landscape';

  if (!query) {
    return res.status(400).json({ error: 'Missing "query" parameter' });
  }

  // Check cache
  const cacheKey = `${query}_${orientation}`;
  const cached = cache.get(cacheKey);
  if (cached && (Date.now() - cached.timestamp) < CACHE_TTL_MS) {
    return res.status(200).json(cached.data);
  }

  try {
    const pexelsData = await fetchPexels(query, orientation);

    if (!pexelsData.photos || pexelsData.photos.length === 0) {
      return res.status(404).json({ error: 'No photos found', imageUrl: null });
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

    cache.set(cacheKey, { data: result, timestamp: Date.now() });
    return res.status(200).json(result);
  } catch (err) {
    console.error('[ERROR] Pexels fetch failed:', err.message);
    return res.status(502).json({ error: err.message, imageUrl: null });
  }
};
