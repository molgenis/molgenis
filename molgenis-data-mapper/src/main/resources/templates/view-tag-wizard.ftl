<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['mapping-service.css', 'select2.css']>
<#assign js=['single-mapping-project.js', 'bootbox.min.js', 'jquery.scroll.table.body.js', 'select2-patched.js', 'tag-wizard.js']>

<@header css js/>

<div class="row">
	<div class="col-md-12">
		<a onclick="window.history.back()" class="btn btn-default"><span class="glyphicon glyphicon-chevron-left"></span> Back to mapping project</a>	
	</div>
</div>

<div class="row">
	<div class="col-md-6">
		<h3>Tag Wizard</h3>
		<p>View and curate auto generated ontology tags</p>
	</div>
</div>

<div class="row">
	<div class="col-md-12">
		<hr></hr>
	</div>
</div>

<div class="row">
	<div class="col-md-6" style="overflow-y:auto;max-height:500px;min-height:500px;">
		<h4>Hypertension</h4>
		<p>This is a really awsome description of this attribute</p>
		
		<h5>
			Tags
			<button type="button" class="btn btn-default btn-xs"><span class="add-tag glyphicon glyphicon-plus"></span></button>
			 
		</h5>	
		<table class="table">
			<thead>
				<th>Expression</th>
				<th>Tags</th>
			</thead>
			<tbody>
				<tr>
					<td>
						<#assign expression = "is_associated_with" />
						<button type="btn" class="btn btn-default btn-xs expression" value="${expression}">${expression}</button>						
					</td>
					<td>
						<button type="btn" class="btn btn-primary btn-xs tag-remove-btn">TAG 1 <span class="glyphicon glyphicon-remove"></span></button></button>
						<button type="btn" class="btn btn-primary btn-xs tag-remove-btn">TAG 2 <span class="glyphicon glyphicon-remove"></span></button></button>
					</td>				
				</tr>
				<tr>
					<td>
						<#assign expression = "is_caused_by" />
						<button type="btn" class="btn btn-default btn-xs expression" value="${expression}">${expression}</button>
					</td>
					<td>
						<button type="btn" class="btn btn-primary btn-xs tag-remove-btn">TAG 5 <span class="glyphicon glyphicon-remove"></span></button></button>
					</td>
				</tr>
			
				<tr>
					<td>
						<#assign expression = "is_related_to" />
						<button type="btn" class="btn btn-default btn-xs expression" value="${expression}">${expression}</button>
					</td>
					<td>
						<button type="btn" class="btn btn-primary btn-xs tag-remove-btn">TAG 4 <span class="glyphicon glyphicon-remove"></span></button>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
	<div id="expression-tag-info-container" class="col-md-6"></div>
</div>

<div class="row">
	<div class="col-md-12">
		<hr></hr>
		<button type="btn" class="btn btn-primary">Reannotate</button>
		<button type="btn" class="btn btn-danger">Clear tags</button>
	</div>
</div>

<script id="expression-and-tag-template" type="text/x-handlebars-template">
	<div class="row">
		<div class="col-md-12">
			<legend>{{this.expression}}</legend>
		</div>
	</div>
	
	<div class="row">
		<div class="col-md-6">
			<select multiple class="form-control" id="tag-dropdown">
				{{#each taglist}}
					<option selected="selected">{{this}}</option>
				{{/each}}
			</select>
		</div>
	</div>
</script>

<@footer/>