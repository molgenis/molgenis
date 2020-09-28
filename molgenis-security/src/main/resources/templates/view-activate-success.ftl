<#include "resource-macros.ftl">
<!DOCTYPE html>
<html>
<head>
    <title>Activation Success</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">
    <link rel="stylesheet" href="<@resource_href "/css/bootstrap-3/${app_settings.bootstrapTheme?html}"/>" type="text/css" id="bootstrap-theme">
</head>
<body class="mg-page">
<div class="container-fluid mg-page-content">
    <div class="jumbotron jumbotron-fluid">
        <div class="container">
            <h3 class="display-4">Your account has been successfully activated</h3>
            <p class="lead">Please Sign in to continue</p>
            <a class="btn btn-primary btn-lg" href="/login" role="button">Sign in</a>
        </div>
    </div>
</div>
</body>
</html>	
