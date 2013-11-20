<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<#if isCurrentUserCanEdit>
<div class="row-fluid">
   <div class="control-group">
		<div class="controls">
			<div class="btn-group">
				<a id="editBtn" href="${editHref}" class="btn">Edit page</a>
			</div>
		</div>
	</div>	
</div>
</#if>
<div class="row-fluid">
	<div class="span12">${content}</div>
</div>
<@footer/>
