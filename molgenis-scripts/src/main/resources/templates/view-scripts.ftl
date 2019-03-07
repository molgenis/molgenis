<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["scripts/chunk-vendors.css", "scripts/app.css"]>
<#assign js=[]>
<#assign version = 2>

<script type="text/javascript">
    window.BaseURL = '${baseUrl}'
</script>

<@header css js version/>
    <div id="molgenis-scripts"></div>
    <script type=text/javascript src="<@resource_href "/js/scripts/chunk-vendors.js"/>"></script>
    <script type=text/javascript src="<@resource_href "/js/scripts/app.js"/>"></script>
<@footer version/>
