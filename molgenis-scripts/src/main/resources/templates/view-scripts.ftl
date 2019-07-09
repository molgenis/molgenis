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

<link rel="stylesheet" href="/ui/@molgenis-ui/scripts/dist/css/app.css"/>
<link rel="stylesheet" href="/ui/@molgenis-ui/scripts/dist/css/chunk-vendors.css"/>

<@header css js version/>
    <div id="molgenis-scripts"></div>
    <script type=text/javascript src="/ui/@molgenis-ui/scripts/dist/js/chunk-vendors.js"></script>
    <script type=text/javascript src="/ui/@molgenis-ui/scripts/dist/js/app.js"></script>
<@footer version/>
