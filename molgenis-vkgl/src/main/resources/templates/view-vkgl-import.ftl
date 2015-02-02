<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=[]>

<@header css js/>

<form name="vkgl-import-form" method="POST">
	<input type="text" name="filename" />
	<input type="submit" value="Import" />
</form>

<@footer/>