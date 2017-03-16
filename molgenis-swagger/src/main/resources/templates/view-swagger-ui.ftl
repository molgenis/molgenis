<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="x-ua-compatible" content="IE=edge">
    <title>Swagger UI</title>
    <link rel="icon" type="image/png" href="/webjars/swagger-ui/images/favicon-32x32.png" sizes="32x32"/>
    <link rel="icon" type="image/png" href="/webjars/swagger-ui/images/favicon-16x16.png" sizes="16x16"/>
    <link href='/webjars/swagger-ui/css/typography.css' media='screen' rel='stylesheet' type='text/css'/>
    <link href='/webjars/swagger-ui/css/reset.css' media='screen' rel='stylesheet' type='text/css'/>
    <link href='/webjars/swagger-ui/css/screen.css' media='screen' rel='stylesheet' type='text/css'/>
    <link href='/webjars/swagger-ui/css/reset.css' media='print' rel='stylesheet' type='text/css'/>
    <link href='/webjars/swagger-ui/css/print.css' media='print' rel='stylesheet' type='text/css'/>

    <script src='/webjars/swagger-ui/lib/object-assign-pollyfill.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/lib/jquery-1.8.0.min.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/lib/jquery.slideto.min.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/lib/jquery.wiggle.min.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/lib/jquery.ba-bbq.min.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/lib/handlebars-4.0.5.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/lib/lodash.min.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/lib/backbone-min.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/swagger-ui.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/lib/highlight.9.1.0.pack.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/lib/highlight.9.1.0.pack_extended.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/lib/jsoneditor.min.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/lib/marked.js' type='text/javascript'></script>
    <script src='/webjars/swagger-ui/lib/swagger-oauth.js' type='text/javascript'></script>

    <script type="text/javascript">
        $(function () {
            var url = "${url}";
            var authorizations = {
                <#if token??>"token": new SwaggerClient.ApiKeyAuthorization("x-molgenis-token", "${token}", "header")</#if>}
            hljs.configure({
                highlightSizeThreshold: 5000
            });

            // Pre load translate...
            if (window.SwaggerTranslator) {
                window.SwaggerTranslator.translate();
            }
            window.swaggerUi = new SwaggerUi({
                url: url,
                authorizations: authorizations,
                dom_id: "swagger-ui-container",
                supportedSubmitMethods: ['get', 'post', 'put', 'delete', 'patch'],
                onComplete: function (swaggerApi, swaggerUi) {
                    if (typeof initOAuth == "function") {
                        initOAuth({
                            clientId: "your-client-id",
                            clientSecret: "your-client-secret-if-required",
                            realm: "your-realms",
                            appName: "your-app-name",
                            scopeSeparator: " ",
                            additionalQueryStringParams: {}
                        });
                    }

                    if (window.SwaggerTranslator) {
                        window.SwaggerTranslator.translate();
                    }
                },
                onFailure: function (data) {
                    log("Unable to Load SwaggerUI");
                },
                docExpansion: "none",
                jsonEditor: false,
                defaultModelRendering: 'schema',
                showRequestHeaders: false,
                showOperationIds: false
            });

            window.swaggerUi.load();

            function log() {
                if ('console' in window) {
                    console.log.apply(console, arguments);
                }
            }
        });
    </script>
</head>

<body class="swagger-section">
<div id='header'>
    <div class="swagger-ui-wrap">
        <a id="logo" href="http://swagger.io"><img class="logo__img" alt="swagger" height="30" width="30"
                                                   src="/webjars/swagger-ui/images/logo_small.png"/><span
                class="logo__title">swagger</span></a>
        <form id='api_selector'>
            <div class='input'><input placeholder="http://example.com/api" id="input_baseUrl" name="baseUrl"
                                      type="text"/></div>
            <div id='auth_container'></div>
            <div class='input'><a id="explore" class="header__btn" href="#" data-sw-translate>Explore</a></div>
        </form>
    </div>
</div>

<div id="message-bar" class="swagger-ui-wrap" data-sw-translate>&nbsp;</div>
<div id="swagger-ui-container" class="swagger-ui-wrap"></div>
</body>
</html>
