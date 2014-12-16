<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["tinymce/tinymce.min.js", "staticcontent.edit.js"]>
<@header css js/>
<div class="row">
	<div class="col-md-12">
		<form id="contentForm" method="post" role="form">
		   
		   <div class="form-group">
                <div class="col-md-8 col-md-offset-2">
        			<#if content?has_content>
        		    	<textarea id="elm1" name="content" form="contentForm" rows="15">${content?html} <#if succes?has_content>${succes?html}</#if></textarea>
        		    <#else>	
        		    	<textarea id="elm1" name="content" form="contentForm" rows="15"></textarea>
        		    </#if>
                </div>
           </div>
		   
		   <div class="form-group">
				<div class="col-md-8 col-md-offset-2">
					<div class="btn-group pull-right">
						<a id="cancelBtn" href="${context_url?html}" class="btn btn-default">Close</a>
						<button id="submitBtn" type="submit" class="btn btn-default">Save</button>
					</div>
				</div>
			</div>

		</form>
	</div>
</div>
<@footer/>
