<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=[]>
<#assign version = 2>

<script type="text/javascript">
    window.__INITIAL_STATE__ = {
        baseUrl: '${baseUrl}',
        lng: '${lng}',
        fallbackLng: '${fallbackLng}'
    }
</script>

<link rel="stylesheet" href="/@molgenis-ui/scripts/dist/css/scripts/app.css"/>
<link rel="stylesheet" href="/@molgenis-ui/scripts/dist/css/scripts/chunk-vendors.css"/>

<@header css js version/>
    <div id="molgenis-scripts"></div>
    <script type=text/javascript src="/@molgenis-ui/scripts/dist/js/scripts/chunk-vendors.js"></script>
    <script type=text/javascript src="/@molgenis-ui/scripts/dist/js/scripts/app.js"></script>
<@footer version/>
