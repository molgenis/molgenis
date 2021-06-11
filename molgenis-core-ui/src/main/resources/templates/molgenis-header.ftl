<#-- write HTML header and plugin menu -->
<#--   css (optional) list of additional stylesheets to include -->
<#--   js  (optional) list of additional js files to include -->
<#include "resource-macros.ftl">
<#include "polyfill-macros.ftl">
<#macro header css=[] js=[] version=1 jsGlobal=[]>
    <#assign cookieWall = app_settings.googleAnalyticsIpAnonymization == false && (app_settings.googleAnalyticsTrackingId?? || app_settings.googleAnalyticsTrackingIdMolgenis??) || (app_settings.googleAnalyticsTrackingId?? && !app_settings.googleAnalyticsAccountPrivacyFriendly) || (app_settings.googleAnalyticsTrackingIdMolgenis?? && !app_settings.googleAnalyticsAccountPrivacyFriendlyMolgenis)>
<!DOCTYPE html>
<html>
<head>
    <title>${app_settings.title?html}</title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">

    <#-- Include browser polyfills before any script tags are inserted -->
    <@polyfill/>
    <#-- Global javascript loaded before require.js -->
    <#list jsGlobal as js_file_name>
        <script type=text/javascript src="${js_file_name?html}"></script>
    </#list>
    <#if !version?? || version == 1>
        <link rel="stylesheet" href="${app_settings.legacyThemeURL?html}" type="text/css" id="bootstrap-theme"/>
        <#-- Bundle of third party JavaScript resources used by MOLGENIS: see minify-maven-plugin in molgenis-core-ui/pom.xml for bundle contents -->
        <script src="/@molgenis-ui/core-ui/dist/js/dist/molgenis-vendor-bundle.js"></script>
        <script src="/@molgenis-ui/core-ui/dist/js/dist/molgenis-global.js"></script>
        <script src="/@molgenis-ui/core-ui/dist/js/dist/molgenis-global-ui.js"></script>
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
        <link rel="stylesheet" href="${app_settings.themeURL?html}" type="text/css" id="bootstrap-theme">


    <#-- Include jQuery v3.3.1 to support bootstrap.js -->
        <script type="text/javascript" src="<@resource_href "/js/bootstrap-4/jquery-3.3.1.min.js"/>"></script>
        <script type="text/javascript" src="<@resource_href "/js/jquery.cookie-1.4.1.min.js"/>"></script>

        <#-- Include the JS bundle for bootstrap 4 which includes popper.js -->
        <script type="text/javascript" src="<@resource_href "/js/bootstrap-4/bootstrap.bundle.min.js"/>"></script>
        <script type="text/javascript" src="/@molgenis-ui/legacy-lib/dist/require.js"></script>
    </#if>

    <#-- Load css specified via settigns -->
    <#if app_settings.cssHref?has_content><link rel="stylesheet" href="<@resource_href "/css/${app_settings.cssHref?html}"/>" type="text/css"></#if>

    <#-- Load css specified by plugins -->
    <#list css as css_file_name>
        <link rel="stylesheet" href="<@resource_href "/css/${css_file_name?html}"/>" type="text/css">
    </#list>

    <#include "molgenis-header-tracking.ftl"><#-- before closing </head> tag -->
</head>

<body class="mg-page<#if (!version?? ||version == 1)> legacy</#if><#if (plugin_id??)> mod-${plugin_id}"</#if>>
    <#if !(version??) || version == 1>
        <#-- Navbar menu -->
        <#if menu_id??>
            <#if !(plugin_id??)>
                <#assign plugin_id="NULL">
            </#if>

            <@topmenu menu plugin_id pluginid_with_query_string/>
        </#if>
    <#else>
        <#-- Render the Vue context components -->
        <div id="molgenis-menu"></div>
        <div id="cookiewall"></div>
        <script>
          <#assign menu=gson.toJson(menu)>

          requirejs.config({
            baseUrl: '/@molgenis-ui/legacy-lib/dist'
          });

          requirejs(["context.umd.min", "vue.min"], function(context, Vue) {
            new Vue({
              render: function(createElement) {
                const propsData = {
                  props: {
                    molgenisMenu: {
                      menu: ${menu},
                      <#if app_settings.logoTopHref??>topLogo: '${app_settings.logoTopHref}', </#if>
                      <#if app_settings.logoTopHref??>topLogoMaxHeight: ${app_settings.logoTopMaxHeight}, </#if>
                      <#if app_settings.logoNavBarHref?has_content>navBarLogo: '${app_settings.logoNavBarHref}', </#if>
                      <#if plugin_id??>selectedPlugin: '${plugin_id}', </#if>
                      authenticated: ${authenticated?c},
                      loginHref: '/login',
                      helpLink: {
                        label: 'Help',
                        href: 'https://molgenis.gitbook.io/molgenis/'
                      }
                    }
                  }
                };
                return createElement(context.default.HeaderComponent, propsData);
              }
            }).$mount('#molgenis-menu');

            <#if cookieWall>
            new Vue ({
              render: function(createElement) {
                return createElement(context.default.CookieWall);
              }
            }).$mount('#cookiewall');
            </#if>
          });
        </script>
    </#if>

<#-- Start application content -->
<div class="container-fluid mg-page-content"
     style="margin-top: <#if app_settings.logoTopHref?? && (!version?? ||version == 1)>${app_settings.logoTopMaxHeight} + 50</#if>px;">
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
    <#if plugin_show_settings_cog>
        <div class="edit-settings">
            <span class="glyphicon glyphicon-cog plugin-settings-btn" aria-hidden="true"></span>
            <div id="plugin-settings-container"></div>
        </div>
    </#if>
<div class="row">
    <div class="col-md-12">
        <div id="plugin-container">
            <#assign plugin_description_key = plugin_id + '_description_text'>
            <#if i18n[plugin_description_key] != "#" + plugin_id + "_description_text#">
                ${i18n[plugin_description_key]}
            </#if>
</#macro>


<#-- Topmenu -->
<#--TODO refactor to remove depency on 'Home'-->
<#macro topmenu menu plugin_id pluginid_with_query_string>
    <nav class="navbar navbar-default navbar-fixed-top" role="navigation">
        <#if app_settings.logoTopHref?has_content>
        <header id="top-logo-banner" style="height: ${app_settings.logoTopMaxHeight}px">
            <a href="/"><img id="logo-top" src="${app_settings.logoTopHref?html}" style="max-height: ${app_settings.logoTopMaxHeight}px;"></a>
        </header>
        </#if>
        <div class="container-fluid">
            <div class="navbar-header">
                <#list menu.items as item>
                    <#if !item.isMenu() && item.label == "Home" && app_settings.logoNavBarHref?has_content>
                        <a class="navbar-brand" href="/menu/${menu.id?html}/${item.url?html}">
                            <img class="img-responsive molgenis-navbar-logo" style="height:2em;font-size:16px;"
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
                        <#if !item.isMenu()>
                            <#if item.label != "Home" || !app_settings.logoNavBarHref?has_content>
                                <#if item.url == pluginid_with_query_string>
                                    <li class="active"><a href="#">${item.label?html}</a></li>
                                <#else>
                                    <li><a href="/menu/${menu.id?url('UTF-8')}/${item.url?html}">${item.label?html}</a>
                                    </li>
                                </#if>
                            </#if>

                        <#-- Dropdown menu items -->
                        <#else>
                            <#assign sub_menu = item>
                            <#assign menu_counter = 0>
                            <li class="dropdown">
                                <a class="dropdown-toggle" data-toggle="dropdown" href="#" role="button"
                                   aria-expanded="false">${item.label?html}<b class="caret"></b></a>

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
                        <a id="manual" href="https://molgenis.gitbook.io/molgenis/" target="_blank">Help</a>
                    </li>
                    <li>
                        <form id="logout-form" class="navbar-form" method="post" action="/logout">
                            <button id="signout-button" type="button" class="btn btn-primary">Sign out</button>
                        </form>
                    </li>
                </ul>

                <script>
                     $("#signout-button").click(function () {
                            $('#logout-form').submit();
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

<#-- dropdown for sub_menu -->
<#macro dropdown sub_menu menu_counter>
    <#assign this_menu_counter = menu_counter + 1>

    <#list sub_menu.items as sub_item>
        <#if !sub_item.isMenu()>
            <li>
                <a <#if this_menu_counter gt 1>style="margin-left: ${this_menu_counter * 12}px;"</#if>
                   href="/menu/${sub_menu.id?url('UTF-8')}/${sub_item.url?html}">${sub_item.label?html}</a>
            </li>
        <#else>
            <li class="dropdown-header disabled sub-menu-${this_menu_counter}" role="presentation">
                <a <#if this_menu_counter gt 1>style="margin-left: ${this_menu_counter * 12}px;"</#if>
                   href="#">${sub_item.label?html}</a>
            </li>

            <@dropdown sub_item this_menu_counter />
            <#assign this_menu_counter = this_menu_counter - 1>
        </#if>
    </#list>
</#macro>