<!DOCTYPE html>
<html>
  <head>
      <meta charset="utf-8">
    <title>biobank-plots</title>
  </head>
  <body>
    <div id="app"></div>
    <script>
      window.__INITIAL_STATE__ = {
        entities: {
          biobanks: 'leiden_biobanks',
          samples: 'leiden_RP'
        }
      }
      // See https://webpack.github.io/docs/configuration.html
      __webpack_public_path__ = '/apps/${app.id}/'
    </script>
    <!-- built files will be auto injected -->
  </body>
</html>
