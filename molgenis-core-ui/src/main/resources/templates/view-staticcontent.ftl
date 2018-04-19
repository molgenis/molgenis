<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>

Welcome ${authentication.name}
<br/>
Your authorities:
<#list authentication.authorities as grantedAuthority>
    ${grantedAuthority.authority}
</#list>
<#if authentication.principal?is_hash && authentication.principal.attributes?is_hash>
<br/>
Your attributes:<br/>
    <#list authentication.principal.attributes as attributename, attributevalue >
        ${attributename}:
        <#if attributevalue??>
            <#if attributevalue?is_boolean>
                ${attributevalue?c}
            <#elseif attributevalue?is_date_like>
                ${attributevalue?datetime}
            <#elseif attributevalue?is_enumerable>
                <#list attributevalue as item>${item}</#list>
            <#elseif attributevalue?is_hash>
                <#list attributevalue as k, v>${k}: ${v}</#list>
            <#else>
                ${attributevalue}
            </#if>
        </#if>

    <br/>
    </#list>
</#if>

<#if content?has_content>
<div class="row">
    <div class="col-md-12">
    <#-- Do *not* HTML escape content else text formatting won't work -->
			${content}
    </div>
</div>
</#if>

<#if isCurrentUserCanEdit?has_content && isCurrentUserCanEdit>
<div class="row">
    <div class="col-md-12">
        <hr></hr>
        <a href="${context_url?html}/edit" class="btn btn-default pull-left">Edit page</a>
    </div>
</div>
</#if>

<@footer/>
