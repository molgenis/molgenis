<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css']>
<@header css />

<h4>${heading}</h4>
<#if message??><p>${message}</p></#if>
<a href="${href}">Go back.</a>

<@footer/>