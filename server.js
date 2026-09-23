const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const HOST = '0.0.0.0';

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.apk': 'application/vnd.android.package-archive',
  '.pdf': 'application/pdf',
  '.txt': 'text/plain; charset=utf-8'
};

const server = http.createServer((req, res) => {
  const urlPath = req.url.split('?')[0];

  // Enable CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // Health check endpoint
  if (urlPath === '/health' || urlPath === '/status') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'ok', app: 'FlowVault', timestamp: Date.now() }));
    return;
  }

  // Serve APK download
  if (urlPath === '/app-debug.apk' || urlPath === '/download/apk' || urlPath === '/flowvault.apk') {
    const apkPaths = [
      path.join(__dirname, '.build-outputs', 'app-debug.apk'),
      path.join(__dirname, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk')
    ];
    let foundApk = null;
    for (const p of apkPaths) {
      if (fs.existsSync(p)) {
        foundApk = p;
        break;
      }
    }
    if (foundApk) {
      const stat = fs.statSync(foundApk);
      res.writeHead(200, {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Length': stat.size,
        'Content-Disposition': 'attachment; filename="FlowVault-v1.0.apk"'
      });
      fs.createReadStream(foundApk).pipe(res);
      return;
    } else {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('APK not found. Please compile the app first.');
      return;
    }
  }

  // Route: Admin dashboard standalone
  if (urlPath === '/admin' || urlPath === '/admin_dashboard.html') {
    const adminHtmlPath = path.join(__dirname, 'dashboard_setup', 'admin_dashboard.html');
    if (fs.existsSync(adminHtmlPath)) {
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      fs.createReadStream(adminHtmlPath).pipe(res);
      return;
    }
  }

  // Route: Default page
  let filePath = path.join(__dirname, urlPath === '/' ? 'index.html' : urlPath);

  // If file doesn't exist, check public/ or dashboard_setup/
  if (!fs.existsSync(filePath)) {
    const alt1 = path.join(__dirname, 'public', urlPath);
    const alt2 = path.join(__dirname, 'dashboard_setup', urlPath);
    if (fs.existsSync(alt1)) filePath = alt1;
    else if (fs.existsSync(alt2)) filePath = alt2;
    else filePath = path.join(__dirname, 'index.html');
  }

  if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    res.writeHead(200, { 'Content-Type': contentType });
    fs.createReadStream(filePath).pipe(res);
  } else {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('Not Found');
  }
});

server.listen(PORT, HOST, () => {
  console.log(`FlowVault Web Server listening on http://${HOST}:${PORT}`);
});
