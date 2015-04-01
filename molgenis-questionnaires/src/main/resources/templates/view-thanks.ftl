<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=[]>

<@header css js/>

<a href="/menu/main/questionnaires" class="btn btn-default btn-md"><span class="glyphicon glyphicon-chevron-left"></span> Back to My questionnaires</a>

<div class="row">
	<div class="col-md-12">
		  <#-- Do *not* HTML escape content -->
		${thankYouText!}
	</div>
</div>

<@footer />