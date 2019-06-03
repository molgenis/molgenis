<#include "resource-macros.ftl">
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = []>
<#assign version = 2>

<@header css js version/>
    <div id="app"></div>

    <script type="text/javascript">
        window.__INITIAL_STATE__ = {
            baseUrl: '${baseUrl}',
            lng: '${lng}',
            fallbackLng: '${fallbackLng}',
            isSuperUser: ${isSuperUser?c}
        }
    </script>

    <link rel="stylesheet" href="/@molgenis-ui/metadata-manager/dist/css/metadata-manager/app.css"/>

    <script type=text/javascript src="<@resource_href "/@molgenis-ui/metadata-manager/dist/js/metadata-manager/manifest.js"/>"></script>
    <script type=text/javascript src="<@resource_href "/@molgenis-ui/metadata-manager/dist/js/metadata-manager/vendor.js"/>"></script>
    <script type=text/javascript src="<@resource_href "/@molgenis-ui/metadata-manager/dist/js/metadata-manager/app.js"/>"></script>

<@footer version/>
