<#include "resource-macros.ftl">
<#assign googleSignIn = authentication_settings.googleSignIn && authentication_settings.signUp && !authentication_settings.signUpModeration>
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
<#if googleSignIn>
    <meta name="google-signin-client_id" content="${authentication_settings.googleAppClientId?html}">
</#if>
    <link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">
    <link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">
    <link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
    <script src="<@resource_href "/js/dist/molgenis-global-ui.js"/>"></script>
    <script src="<@resource_href "/js/dist/molgenis-vendor-bundle.js"/>"></script>
    <script src="<@resource_href "/js/dist/molgenis-global.js"/>"></script>
    <script src="<@resource_href "/js/jquery.validate.min.js"/>"></script>
    <script src="<@resource_href "/js/handlebars.min.js"/>"></script>
    <script src="<@resource_href "/js/molgenis.js"/>"></script>

<#if googleSignIn>
<#-- Include script tag before platform.js script loading, else onLoad could be called before the onLoad function is available -->
    <script>
        function onLoad() {
            gapi.load('auth2', function () {
                gapi.auth2.init()
            })
        }
    </script>
    <script src="https://apis.google.com/js/platform.js?onload=onLoad" async defer></script>
</#if>
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
