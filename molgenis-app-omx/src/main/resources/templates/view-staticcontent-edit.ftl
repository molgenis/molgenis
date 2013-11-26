<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["tinymce/tinymce.min.js", "staticcontent.edit.js"]>
<@header css js/>
<div class="row-fluid">
	<div class="span12">
		<form id="contentForm" method="post">
		<div class="row-fluid">
			<#if cancelHref?has_content>
		   <div class="control-group">
				<div class="controls">
					<div class="btn-group">			
						<a id="cancelBtn" href="${cancelHref}" class="btn">Cancel</a>
						<button id="submitBtn" type="submit" class="btn">Save</a>
					</div>
				</div>
			</div>
			</#if>	
		</div>
		<div class="row-fluid">
			<#if content?has_content>
		    <textarea id="elm1" name="content" form="contentForm" rows="15">${content} <#if succes?has_content>${succes}</#if></textarea>
		    </#if>
		</div>    
		</form>
	</div>
</div>
<@footer/>
