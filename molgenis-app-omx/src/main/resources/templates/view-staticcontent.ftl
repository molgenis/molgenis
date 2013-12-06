<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<#if isCurrentUserCanEdit?has_content && isCurrentUserCanEdit>
<div class="row-fluid">
   <div class="control-group">
		<div class="controls">
			<div class="btn-group">
			<#if editHref?has_content>
				<a id="editBtn" href="${editHref}" class="btn">Edit page</a>
			</#if>
			</div>
		</div>
	</div>	
</div>
</#if>
<#if content?has_content>
<div class="row-fluid">
	<div class="span12">${content}</div>
</div>
</#if>
<@footer/>
