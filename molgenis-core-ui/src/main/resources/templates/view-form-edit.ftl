<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#import "molgenis-input-elements.ftl" as input>

<#assign css=['select2.css', 'molgenis-form.css']>
<#assign js=['select2.min.js', 'molgenis-form-edit.js']>

<@header css js/>

<form class="form-horizontal" id="entity-form" method="POST" action="/api/v1/${form.metaData.name?lower_case}/${form.primaryKey}">
	<a href="${context_url}/${form.metaData.name}" class="pull-left">
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
  					<button type="button" class="close" data-dismiss="alert">&times;</button>
  					<strong>${form.title} saved.</strong>
				</div>
			</div>
		</div>
		
		<div id="error-message" class="control-group" style="display: none">
    		<div class="controls">
				<div class="alert alert-error">
  					<button type="button" class="close" data-dismiss="alert">&times;</button>
  					<strong>Error saving ${form.title}</strong>: <span id="error-message-content"></span>
				</div>
			</div>
		</div>
		
		<input type="hidden" name="_method" value="PUT" />
		
		<#list form.metaData.fields as field>
			<@input.render field form.entity />
    	</#list>
    	
    	<div class="control-group">
    		<div class="controls">
      			<button type="submit" class="btn btn-large">Save</button>
    		</div>
  		</div>
  		
	</div>
	
</form>

<#include "view-form-meta.ftl">

<@footer/>