<#-- write HTML header and plugin menu -->
<#--   css (optional) list of additional stylesheets to include -->
<#--   js  (optional) list of additional js files to include -->
<#include "resource-macros.ftl">
<#include "theme-macros.ftl">
<#include "polyfill-macros.ftl">
<#macro header css=[] js=[] version=1>
    <#assign cookieWall = app_settings.googleAnalyticsIpAnonymization == false && (app_settings.googleAnalyticsTrackingId?? || app_settings.googleAnalyticsTrackingIdMolgenis??) || (app_settings.googleAnalyticsTrackingId?? && !app_settings.googleAnalyticsAccountPrivacyFriendly) || (app_settings.googleAnalyticsTrackingIdMolgenis?? && !app_settings.googleAnalyticsAccountPrivacyFriendlyMolgenis)>
    <#assign googleSignIn = authentication_settings.googleSignIn && authentication_settings.signUp && !authentication_settings.signUpModeration>
<!DOCTYPE html>
<html>
<head>
    <title>${app_settings.title?html}</title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <#if googleSignIn><meta name="google-signin-client_id" content="${authentication_settings.googleAppClientId?html}"></#if>
    <link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">

    <#if !version?? || version == 1>
        <link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">
        <link rel="stylesheet" href="<@theme_href "/css/bootstrap-3/${app_settings.bootstrapTheme?html}"/>" type="text/css" id="bootstrap-theme">
        <link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
        <#if app_settings.logoTopHref?has_content><link rel="stylesheet" href="<@resource_href "/css/molgenis-top-logo.css"/>" type="text/css"></#if>

        <#if app_settings.cssHref?has_content><link rel="stylesheet" href="<@resource_href "/css/${app_settings.cssHref?html}"/>" type="text/css"></#if>

        <#-- Include browser polyfills before any script tags are inserted -->
        <@polyfill/>

        <#-- Bundle of third party JavaScript resources used by MOLGENIS: see minify-maven-plugin in molgenis-core-ui/pom.xml for bundle contents -->
        <script src="<@resource_href "/js/dist/molgenis-vendor-bundle.js"/>"></script>
        <script src="<@resource_href "/js/dist/molgenis-global.js"/>"></script>
        <script src="<@resource_href "/js/dist/molgenis-global-ui.js"/>"></script>
        <script src="<@resource_href "/js/jquery.validate.min.js"/>"></script>
        <script src="<@resource_href "/js/handlebars.min.js"/>"></script>
        <script src="<@resource_href "/js/molgenis.js"/>"></script>
        <script src="<@resource_href "/js/script-evaluator.js"/>"></script>

        <#-- Load custome javascript -->
        <#if app_settings.customJavascript?has_content>
            <#list app_settings.customJavascript?split(r"\s*,\s*", "r") as js_file_name>
                <#if js_file_name?ends_with(".js")>
                    <script src="<@resource_href "${js_file_name?html}"/>"></script>
                </#if>
            </#list>
        </#if>

        <#if googleSignIn>
            <#if authenticated?? && authenticated>
            <#-- Include script tag before platform.js script loading, else onLoad could be called before the onLoad function is available -->
                <script>
                    function onLoad() {
                        gapi.load('auth2', function () {
                            gapi.auth2.init();
                        });
                    }
                </script>
            </#if>

            <script src="https://apis.google.com/js/platform.js<#if authenticated?? && authenticated>?onload=onLoad</#if>" async defer></script>
        </#if>

        <script>
            top.molgenis.setCookieWall(${cookieWall?string('true', 'false')});
                <#if context_url??>
                top.molgenis.setContextUrl('${context_url?js_string}');
                </#if>
                <#if plugin_id??>
                top.molgenis.setPluginId('${plugin_id?js_string}');
                </#if>
        </script>

        <#-- Load javascript specified by plugins -->
        <#list js as js_file_name>
            <script src="<@resource_href "/js/${js_file_name?html}"/>"></script>
        </#list>
    <#else>
        <#-- Include bootstrap 4 theme CSS for Vue plugins -->
        <link rel="stylesheet" href="<@theme_href "/css/bootstrap-4/${app_settings.bootstrapTheme?html}"/>" type="text/css" id="bootstrap-theme">

        <#-- Add spacing to body to show content below fixed-top navigation bar -->
        <link rel="stylesheet" href="<@resource_href "/css/bootstrap-4/menu/fixed-top-menu.css"/>" type="text/css">

        <#-- Make footer sticky -->
        <link rel="stylesheet" href="<@resource_href "/css/bootstrap-4/footer/sticky-footer.css"/>" type="text/css">

        <#-- Include jQuery v3.3.1 to support bootstrap.js -->
        <script type="text/javascript" src="<@resource_href "/js/bootstrap-4/jquery-3.3.1.min.js"/>"></script>

        <#-- Include the JS bundle for bootstrap 4 which includes popper.js -->
        <script type="text/javascript" src="<@resource_href "/js/bootstrap-4/bootstrap.bundle.min.js"/>"></script>
    </#if>

    <#-- Load css specified by plugins -->
    <#list css as css_file_name>
        <link rel="stylesheet" href="<@resource_href "/css/${css_file_name?html}"/>" type="text/css">
    </#list>

    <#include "molgenis-header-tracking.ftl"><#-- before closing </head> tag -->
</head>

<body>
    <#if !(version??) || version == 1>
        <#-- Navbar menu -->
        <#if menu_id??>
            <#if !(plugin_id??)>
                <#assign plugin_id="NULL">
            </#if>

            <@topmenu molgenis_ui.getMenu() plugin_id pluginid_with_query_string/>
        </#if>
    <#else>
        <#assign menu=molgenis_ui.getMenuJson()>

        <#-- VUE -->
        <div id="molgenis-menu"></div>

        <script type="text/javascript">
            window.molgenisMenu = {
                menu: ${menu}
                <#if app_settings.logoTopHref??>, topLogo: '${app_settings.logoTopHref}'</#if>
                <#if app_settings.logoNavBarHref?has_content>, navBarLogo: '${app_settings.logoNavBarHref}'</#if>
                <#if plugin_id??>, selectedPlugin: '${plugin_id}'</#if>
                , authenticated: ${authenticated?c}
                , loginHref: '/login'
                <#if googleSignIn>, logoutFunction: function () {
                    var auth2 = gapi.auth2.getAuthInstance()
                    auth2.signOut()
                }</#if>
                , googleSignIn: ${googleSignIn?c}
                , helpLink: {label: 'Help', href: 'https://molgenis.gitbooks.io/molgenis/content/'}
            }
        </script>

        <#-- Include the Vue version of the molgenis menu  -->
        <script type=text/javascript src="<@resource_href "/js/bootstrap-4/menu/molgenis-menu.js"/>"></script>
    </#if>

<#-- Start application content -->
<div class="container-fluid">
    <div class="row">
        <div class="col-md-12">
            <div id="login-modal-container-header"></div>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <div class="alerts">
                <#if errorMessage??>
                    <#assign message = errorMessage>
                    <#assign messageType = "danger">
                <#elseif warningMessage??>
                    <#assign message = warningMessage>
                    <#assign messageType = "warning">
                <#elseif successMessage??>
                    <#assign message = successMessage>
                    <#assign messageType = "success">
                <#elseif infoMessage??>
                    <#assign message = infoMessage>
                    <#assign messageType = "info">
                </#if>

                <#if messageType??>
                    <div class="alert alert-${messageType}">
                        <button type="button" class="close" data-dismiss="alert">&times;</button>
                        <strong>${messageType?capitalize}!</strong> ${message?html}</div>
                </#if>
            </div>
        </div>
    </div>
    <#if plugin_settings_can_write?? && plugin_settings_can_write>
        <div class="row">
            <div class="col-md-12">
                <span class="glyphicon glyphicon-cog pull-right plugin-settings-btn" aria-hidden="true"
                      style="cursor: pointer; margin-bottom: 5px;"></span>
                <div id="plugin-settings-container"></div>
            </div>
        </div>
    </#if>
<div class="row">
    <div class="col-md-12">
        <div id="plugin-container">
            <#if app_settings.logoTopHref?has_content>
                <script>
                    // Calculate the amount of pixels that the content needs to
                    // be pushed down based on the height of the uploaded banner
                    var maxHeight = 0

                    function setContainerHeight() {
                        var img = document.getElementById('logo-top');
                        img.style['height'] = 'auto'

                        <#if app_settings.fixedHeightLogo??>
                        img.style['max-height'] = '${app_settings.fixedHeightLogo?string}px'
                        </#if>

                        var height = img.height
                        maxHeight = height + 60

                        var container = document.querySelector('body>.container-fluid')
                        container.setAttribute("style", "padding-top: " + maxHeight + "px;")
                    }

                    $(window).resize(function() {
                        setContainerHeight()
                    })

                    setContainerHeight()
                </script>
            </#if>

            <#assign plugin_description_key = plugin_id + '_description_text'>
            <#if i18n[plugin_description_key] != "#" + plugin_id + "_description_text#">
                ${i18n[plugin_description_key]}
            </#if>
</#macro>


<#-- Topmenu -->
<#--TODO refactor to remove depency on 'Home'-->
<#macro topmenu menu plugin_id pluginid_with_query_string>
    <nav class="navbar navbar-default navbar-fixed-top" style="margin-bottom: 10px" role="navigation">
        <div class="container-fluid">
            <#if app_settings.logoTopHref?has_content>
            <header id="top-logo-banner">
                <a href="/"><img id="logo-top" src="${app_settings.logoTopHref?html}" alt="" border="0"></a>
            </header>
            </#if>
            <div class="navbar-header">
                <#list menu.items as item>
                    <#if item.type != "MENU" && item.name == "Home" && app_settings.logoNavBarHref?has_content>
                        <a class="navbar-brand" href="/menu/${menu.id?html}/${item.url?html}">
                            <img class="img-responsive" style="max-width:100%;max-height:100%;"
                                 src="${app_settings.logoNavBarHref?html}"
                                 alt="${app_settings.title?html}">
                        </a>
                        <button type="button" class="navbar-toggle" data-toggle="collapse"
                                data-target="#bs-molgenis-navbar">
                            <span class="sr-only">Toggle navigation</span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                    </#if>
                </#list>
            <#-- Logo end -->
            </div>

        <#-- Navbar items start -->
            <div class="collapse navbar-collapse" id="bs-molgenis-navbar">
                <ul class="nav navbar-nav">
                    <#list menu.items as item>

                    <#-- Single menu items -->
                        <#if item.type != "MENU">
                            <#if item.name != "Home" || !app_settings.logoNavBarHref?has_content>
                                <#if item.url == pluginid_with_query_string>
                                    <li class="active"><a href="#">${item.name?html}</a></li>
                                <#else>
                                    <li><a href="/menu/${menu.id?url('UTF-8')}/${item.url?html}">${item.name?html}</a>
                                    </li>
                                </#if>
                            </#if>

                        <#-- Dropdown menu items -->
                        <#elseif item.type == "MENU">
                            <#assign sub_menu = item>
                            <#assign menu_counter = 0>
                            <li class="dropdown">
                                <a class="dropdown-toggle" data-toggle="dropdown" href="#" role="button"
                                   aria-expanded="false">${item.name?html}<b class="caret"></b></a>

                                <ul class="dropdown-menu" role="menu">
                                    <@dropdown sub_menu menu_counter />
                                </ul>
                            </li>
                        </#if>
                    </#list>
                </ul>

                <#if authenticated?? && authenticated>

                <ul class="nav navbar-nav navbar-right" >
                    <li>
                        <form class="navbar-form">
                            <div id="language-select-box"></div>
                        </form>
                    </li>
                    <li>
                        <a id="manual" href="https://molgenis.gitbooks.io/molgenis/content/" target="_blank">Help</a>
                    </li>
                    <li>
                        <form id="logout-form" class="navbar-form" method="post" action="/logout">
                            <button id="signout-button" type="button" class="btn btn-primary">Sign out</button>
                        </form>
                    </li>
                </ul>

                <script>
                     $("#signout-button").click(function () {
                    <#if googleSignIn>
                        var auth2 = gapi.auth2.getAuthInstance();
                        auth2.signOut().then(function () {
                    </#if>
                            $('#logout-form').submit();
                    <#if googleSignIn>
                        });
                    </#if>
                     });
                </script>

                <#else>
                <form class="navbar-form navbar-right" method="post" action="/login">
                    <a id="open-button" type="btn" class="btn btn-default" data-toggle="modal"
                       data-target="#login-modal">Sign in</a>
                </form>
                </#if>


            </div>
        <#-- Navbar items end -->

        </div> <#-- close container -->
    </nav> <#-- close navbar -->
</#macro>

<#-- dropdown for entity -->
<#macro dropdown sub_menu menu_counter>
    <#assign this_menu_counter = menu_counter + 1>

    <#list sub_menu.items as sub_item>
        <#if sub_item.type != "MENU">
            <li>
                <a <#if this_menu_counter gt 1>style="margin-left: ${this_menu_counter * 12}px;"</#if>
                   href="/menu/${sub_menu.id?url('UTF-8')}/${sub_item.url?html}">${sub_item.name?html}</a>
            </li>
        <#elseif sub_item.type == "MENU">
            <li class="dropdown-header disabled sub-menu-${this_menu_counter}" role="presentation">
                <a <#if this_menu_counter gt 1>style="margin-left: ${this_menu_counter * 12}px;"</#if>
                   href="#">${sub_item.name?html}</a>
            </li>

            <@dropdown sub_item this_menu_counter />
            <#assign this_menu_counter = this_menu_counter - 1>
        </#if>
    </#list>
</#macro>
