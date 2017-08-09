<#include "resource-macros.ftl">
<!doctype html>
<html lang="en">
<head>
    <title>Meta data manager</title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="shortcut icon" href="/js/favicon.ico">
    <link href="<@resource_href "/css/metadata-manager/app.css" />" rel="stylesheet">
</head>
<body>
    <div id="app"></div>

    <script type="text/javascript">
        window.__INITIAL_STATE__ = {
            baseUrl: '${baseUrl}',
            lng: '${lng}',
            fallbackLng: '${fallbackLng}'
        }
    </script>

    <script type=text/javascript src="<@resource_href "/js/metadata-manager/manifest.js"/>"></script>
    <script type=text/javascript src="<@resource_href "/js/metadata-manager/vendor.js"/>"></script>
    <script type=text/javascript src="<@resource_href "/js/metadata-manager/app.js"/>"></script>
</body>
</html>