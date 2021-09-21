<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = []>
<#assign css = []>
<#assign version = 2>

<link rel="stylesheet" href="/@molgenis-ui/data-row-permissions/dist/css/chunk-vendors.css"/>
<link rel="stylesheet" href="/@molgenis-ui/data-row-permissions/dist/css/app.css"/>

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

<script type=text/javascript src="/@molgenis-ui/data-row-permissions/dist/js/chunk-vendors.js"></script>
<script type=text/javascript src="/@molgenis-ui/data-row-permissions/dist/js/app.js"></script>

<@footer version/>