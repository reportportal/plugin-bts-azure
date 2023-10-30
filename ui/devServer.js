const http = require('http');
const staticServer = require('node-static');

const contentPath = './build/public';
const headers = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': '*',
  'Access-Control-Allow-Headers': '*',
  'Cache-Control': 'no-cache, no-store, must-revalidate, max-age=0',
};
const file = new staticServer.Server(contentPath, { headers });

http
  .createServer((req, res) => {
    console.log(`Request URL: ${req.url}`);
    file.serve(req, res);
  })
  .listen(9090, () => {
    console.log(`Serving files from ${contentPath}`);
  });
