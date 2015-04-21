<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['select2.css', 'bootstrap-datetimepicker.min.css', 'molgenis-form.css', 'questionnair.css']>
<#assign js=['jquery.validate.min.js', 'select2-patched.js', 'moment-with-locales.min.js', 'bootstrap-datetimepicker.min.js','questionnair.js']>

<@header css js/>

<a href="/menu/main/questionnaires" class="btn btn-default btn-md"><span class="glyphicon glyphicon-chevron-left"></span> Back to My questionnaires</a>

<div class="row">		
	<div class="col-md-6">
		<h1>${questionnair.label!?html}</h1>
		<p>${questionnair.description!?html}</p>
		<legend></legend>
	</div>  	
</div>

<div class="row">
	<div id="form-holder" data-name="${questionnair.name!?url('UTF-8')}" data-id="${questionnair.id!}" class="col-md-6"></div>
</div>

<@footer />