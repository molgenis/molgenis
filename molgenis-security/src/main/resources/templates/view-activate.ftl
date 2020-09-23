<#include "resource-macros.ftl">
<!DOCTYPE html>
<html>
<head>
    <title>Activate account</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">
    <link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">
    <link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
</head>
<body class="mg-page">
<div class="container-fluid mg-page-content">
    <div class="jumbotron jumbotron-fluid">
        <div class="container">
            <h1>Activate Account</h1>
            <form action="/account/activate" method="post">
                <div class="form-group">
                    <label for="activation-code">Code</label>
                    <input type="text" class="form-control" id="activation-code" name="activationCode" placeholder="Activation code">
                </div>
                <button type="submit" class="btn btn-success">Activate</button>
                <#if errorMessage?has_content>
                    <div id="error-box" class="alert alert-danger" role="alert" style="margin-top: 1rem;">
                        ${errorMessage}
                    </div>
                </#if>
            </form>
        </div>
    </div>
</div>
</body>
</html>	
