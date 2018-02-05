/* eslint-env node */
'use strict';

const proxyPath = '/api/operations/v1/:opId';
const proxyPathAll = '/api/operations/v1/';

module.exports = function (app) {
  // For options, see:
  // https://github.com/nodejitsu/node-http-proxy
  let proxy = require('http-proxy').createProxyServer({});

  proxy.on('error', function (err, req) {
    console.error(err, req.url);
  });

  app.use(proxyPath, function (req, res, next) {
    // include root path in proxied request
    req.url = proxyPath + '/' + req.url;
    proxy.web(req, res, {target: 'http://localhost:7800/operations/v1/'+req.params.opId, ignorePath: true});
  });
  app.use(proxyPathAll, function (req, res, next) {
    // include root path in proxied request
    req.url = proxyPath + '/' + req.url;
    proxy.web(req, res, {target: 'http://localhost:7800/operations/v1/', ignorePath: true});
  });
};
