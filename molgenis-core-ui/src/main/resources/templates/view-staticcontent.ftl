<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>

<div class="row">
	<div class="col-md-11">
		<#if content?has_content>
			${content}
		</#if>
	</div>
	<div class="col-md-1">
		<#if isCurrentUserCanEdit?has_content && isCurrentUserCanEdit>
			<a href="${context_url}/edit" class="btn btn-default pull-right">Edit page</a>
		</#if>
	</div>
</div>

<@footer/>
