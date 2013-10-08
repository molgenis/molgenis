<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#import "form-macros.ftl" as f>

<#assign css=['molgenis-form.css']>
<#assign js=['molgenis-form-list.js', 'jquery.bootstrap.pager.js']>

<@header css js/>

<script>
	var forms = [];
	var CURRENT_URI = '${current_uri}';
</script>

<div id="success-message" class="alert alert-success" style="display: none">
 	<button type="button" class="close">&times;</button>
  	<strong id="success-message-content"></strong>
</div>

<div id="error-message" class="alert alert-error" style="display: none">
	<button type="button" class="close">&times;</button>
  	<strong id="error-message-content"></strong>
</div>
			

<@f.renderList form 0 />
<@f.meta form 0 />

<#list form.subForms as subForm>
	<@f.renderList subForm subForm_index+1 />
	<@f.meta subForm subForm_index+1 />
</#list>

	
<@footer/>