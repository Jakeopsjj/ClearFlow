/**
 * Vercel Serverless Function: Health Check
 * GET /api/health
 */
module.exports = (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.status(200).json({
    status: 'ok',
    timestamp: Date.now(),
    service: 'cleardu-pexels-proxy',
    platform: 'vercel'
  });
};
