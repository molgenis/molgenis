<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["tinymce/tinymce.min.js", "staticcontent.edit.js"]>
<@header css js/>
<div class="row">
	<div class="col-md-12">
		<form id="contentForm" method="post" role="form">
		<div class="row">
		   <div class="form-group">
				<div class="col-md-9">
					<div class="btn-group">			
						<a id="cancelBtn" href="${context_url}" class="btn btn-default">Cancel</a>
						<button id="submitBtn" type="submit" class="btn btn-default">Save</a>
					</div>
				</div>
			</div>
		</div>
		<div class="row">
			<#if content?has_content>
		    <textarea id="elm1" name="content" form="contentForm" rows="15">${content} <#if succes?has_content>${succes}</#if></textarea>
		    </#if>
		</div>    
		</form>
	</div>
</div>
<@footer/>
