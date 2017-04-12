<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["indexmanager.js"]>
<@header css js/>
<#if entities?has_content>
<form id="reindex-form" method="post" name="reindex-form" action="${context_url?html}/reindex" role="form">
    <#list entities as entity>
        <div class="radio">
            <label>
                <input type="radio" name="type" value="${entity.id?html}"<#if entity_index == 0>
                       checked</#if>>${entity.label?html}
            </label>
        </div>
    </#list>
    <button type="submit" class="btn btn-default">Reindex</button>
</form>
<#else>
<span>No indexed entities.</span>
</#if>
<@footer/>