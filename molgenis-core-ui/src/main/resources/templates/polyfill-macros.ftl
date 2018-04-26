<#-- Emulate a full ES2015+ environment for older browsers (must be the first script loaded) -->
<#include "resource-macros.ftl"><#macro polyfill><#if environment.environmentType == "production"><#assign polyfill_js_name = "polyfill.min.js"><#else><#assign polyfill_js_name = "polyfill.js"></#if>
<script src="<@resource_href "/js/${polyfill_js_name}"/>"></script>
</#macro>