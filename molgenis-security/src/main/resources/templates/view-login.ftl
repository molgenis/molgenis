<#include "resource-macros.ftl">
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">
    <link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">
    <link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
    <script src="<@resource_href "/js/dist/molgenis-global-ui.js"/>"></script>
    <script src="<@resource_href "/js/dist/molgenis-vendor-bundle.js"/>"></script>
    <script src="<@resource_href "/js/dist/molgenis-global.js"/>"></script>
    <script src="<@resource_href "/js/jquery.validate.min.js"/>"></script>
    <script src="<@resource_href "/js/handlebars.min.js"/>"></script>
    <script src="<@resource_href "/js/molgenis.js"/>"></script>
</head>
<body>
<#assign disableClose="true">
<#include "/login-modal.ftl">
<script type="text/javascript">
    $(function () {
        $('#login-modal').modal({backdrop: 'static'})
    <#if errorMessage??>
        $('#alert-container').html($('<div class="alert alert-block alert-danger alert-dismissable fade in"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><strong>Warning!</strong> ${errorMessage?html}</div>'))
    </#if>
    })
</script>
</body>
</html>	
