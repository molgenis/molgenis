<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<#if isCurrentUserCanEdit?has_content && isCurrentUserCanEdit>
<div class="row">
    <a href="${context_url}/edit" class="btn btn-default pull-right">Edit page</a>
</div>
</#if>
<#if content?has_content>
<div class="row">
	<div class="col-md-12">${content}</div>
</div>
</#if>
<@footer/>
