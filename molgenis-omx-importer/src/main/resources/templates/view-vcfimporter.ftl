<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["vcfimporter.js"]>
<@header css js/>
<form name="vcf-importer-form" class="form-horizontal" action="${context_url}/import" method="POST" enctype="multipart/form-data">
	<input type="file" name="file" required>
	<button type="submit" class="btn">Import</button> 
</form>
<@footer/>