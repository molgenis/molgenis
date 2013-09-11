<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#import "molgenis-input-elements.ftl" as input>
<#import "form-macros.ftl" as f>

<#assign css=['select2.css', 'molgenis-form.css']>
<#assign js=['jquery.validate.min.js', 'select2.min.js', 'molgenis-form-edit.js']>

<@header css js/>

<script>
	var forms = [];
</script>

<form class="form-horizontal" id="entity-form" method="POST" action="/api/v1/${form.metaData.name?lower_case}<#if form.primaryKey??>/${form.primaryKey}</#if>">
	<a href="javascript:history.go(-1)" class="pull-left">
		<div id="back">
			<div class="nav-icon-prev pull-left"></div>
			<div class="nav-icon-prev pull-left"></div>
			<div class="back-text pull-left">Back to list</div>
		</div>
		<div class="clearfix"></div>
	</a>
	
	<div class="pull-left">
		<div id="success-message" class="control-group" style="display: none">
    		<div class="controls">
				<div class="alert alert-success">
  					<button type="button" class="close">&times;</button>
  					<strong>${form.title} saved.</strong>
				</div>
			</div>
		</div>
		
		<div id="error-message" class="control-group" style="display: none">
    		<div class="controls">
				<div class="alert alert-error">
  					<button type="button" class="close">&times;</button>
  					<strong>Error saving ${form.title}</strong>: <span id="error-message-content"></span>
				</div>
			</div>
		</div>
		
		<#if form.primaryKey??>
			<input type="hidden" name="_method" value="PUT" >
		<#else>
			<input type="hidden" name="_method" value="" >
		</#if>
		
		<#list form.metaData.fields as field>
			<#if form.entity??>
				<@input.render field form.hasWritePermission form.entity />
			<#else>
				<@input.render field form.hasWritePermission />
			</#if>
    	</#list>
    	
    	<#if form.hasWritePermission>
    		<div class="control-group">
    			<div class="controls">
      				<button type="submit" class="btn btn-large pull-right">Save</button>
    			</div>
  			</div>
  		</#if>
  		
	</div>
	
</form>

<@f.remoteValidationRules form />

<@footer/>