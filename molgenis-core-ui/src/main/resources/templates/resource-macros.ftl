<#-- Generates resource href by -->
<#-- * appending resource name with automatically generated version number (this fingerprint avoids the use of stale browser data)-->
<#-- * selection minified or unminified css/js based on environment -->
<#macro resource_href resource_name>
    <#if environment == "production">
        <#-- changes to excludes must be applied to pom.xml -->
        <#if resource_name?ends_with(".css") && !resource_name?ends_with(".min.css")>
            <#assign filtered_resource_name = resource_name?replace(".css", ".min.css")> 
        <#-- changes to excludes must be applied to pom.xml -->
        <#elseif resource_name?ends_with(".js") && !resource_name?ends_with(".min.js") && !resource_name?contains("/src-min-noconflict/")>
            <#assign filtered_resource_name = resource_name?replace(".js", ".min.js")>
        <#else>
            <#assign filtered_resource_name = resource_name>
        </#if>
    <#else>
        <#assign filtered_resource_name = resource_name>
    </#if>${filtered_resource_name?html}?${resource_fingerprint_registry.getFingerprint(filtered_resource_name)?html}</#macro>