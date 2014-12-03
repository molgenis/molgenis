<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#import "molgenis-input-elements.ftl" as input>
<#import "form-macros.ftl" as f>

<#assign css=['select2.css', 'bootstrap-datetimepicker.min.css', 'molgenis-form.css']>
<#assign js=['jquery.validate.min.js', 'select2.min.js', 'moment-with-locales.min.js', 'bootstrap-datetimepicker.min.js', 'molgenis-form-edit.js', 'ace/src-min-noconflict/ace.js']>

<@header css js/>

<script>
	var forms = [];
</script>

<div id="success-message" class="form-group" style="display: none">
	<div class="col-md-12">
		<div class="alert alert-success">
			<button type="button" class="close">&times;</button>
			<strong>${form.title?html} saved.</strong>
		</div>
	</div>
</div>

<#if back??>
	<a href="${back?html}" class="btn btn-default btn-md"><span class="glyphicon glyphicon-chevron-left"></span> Back to list</a>
</#if>

<form role="form" class="form-horizontal" id="entity-form" method="POST" action="/api/v1/${form.metaData.name?lower_case?html}<#if form.primaryKey??><#if form.primaryKey?is_number>/${form.primaryKey?c?html}<#else>/${form.primaryKey?html}</#if></#if>">
	<div class="form-group">
		<div class="col-md-3">
			<#if form.primaryKey??>
				<input type="hidden" name="_method" value="PUT" >
			<#else>
				<input type="hidden" name="_method" value="" >
			</#if>
		</div>
	</div>

	<div class="form-group">
		<div class="col-md-5">
			<legend>Required</legend>
			<#assign optionalCounter = 0 />
			<#list form.metaData.fields as field>	
				<#if !field.nillable>
					<#if form.entity??>
						<@input.render field form.hasWritePermission form.entity form.metaData.forUpdate/>
					<#else>
						<@input.render field form.hasWritePermission '' form.metaData.forUpdate/>
					</#if>
				<#else>
					<#assign optionalCounter = optionalCounter + 1>
				</#if>
			</#list>
			
			<#if optionalCounter gt 0>
				<legend>Optional</legend>
				<#list form.metaData.fields as field>
					<#if field.nillable && field.visible>
						<#if form.entity??>
							<@input.render field form.hasWritePermission form.entity form.metaData.forUpdate/>
						<#else>
							<@input.render field form.hasWritePermission '' form.metaData.forUpdate/>
						</#if>
					</#if>
				</#list>
			</#if>
		</div>
	</div>
	
	<#if form.hasWritePermission>
		<div class="form-group">
			<div class="col-md-5">
				<button type="submit" class="btn btn-large btn-primary pull-right">Save</button>
			</div>
		</div>
	</#if>
</form>

<@f.remoteValidationRules form />

<@footer/>