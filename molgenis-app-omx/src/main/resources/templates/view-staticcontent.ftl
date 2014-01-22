<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<#if isCurrentUserCanEdit?has_content && isCurrentUserCanEdit>
<div class="row-fluid">
   <div class="control-group">
		<div class="controls">
			<div class="btn-group">
				<ul>
					<li>
						<form action="${context_url}/upload-logo" method="POST" enctype="multipart/form-data">
							<input type="file" name="logo" />
							<input type="submit" value="Upload logo" class="btn" />
						</form>
					</li>
					<li><a id="editBtn" href="${context_url}/edit" class="btn">Edit page</a></li>
				<ul>
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
