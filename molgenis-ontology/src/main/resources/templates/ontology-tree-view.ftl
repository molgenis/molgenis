<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["bootstrap.fileupload.min.css", "ui.fancytree.min.css", "biobank-connect.css"]>
<#assign js=["bootstrap-fileupload.min.js", "jquery.fancytree.min.js", "common-component.js", "ontology.tree.plugin.js", "ontology-tree-view.js"]>
<@header css js/>
<form class="form-horizontal" enctype="multipart/form-data">
	<div class="row">
		<br>
		<div class="col-md-offset-3 col-md-6">
			<legend>
				<h3><center>Ontology View</center></h3>
			</legend>
		</div>
	</div>
	<#if ontologies?? && (ontologies?size > 0)>
	<div class="row">
		<div class="col-md-6">
			<div class="row">
				<div class="col-md-5">
					<input id="searchField" type="text" class="form-control">
				</div>
				<div class="btn-group">
					<button id="searchButton" type="text" class="btn btn-primary">Search</button>
					<button id="clearButton" type="text" class="btn btn-default">Clear</button>
				</div>
			</div>
		</div>
		<div class="col-md-offset-4 col-md-2">
			<select id="selectOntologies">
				<#list ontologies as ontology>
					<option value="${ontology.ontologyIRI?html}">${ontology.ontologyName?html}</option>
				</#list>
			</select>
		</div>
	</div>
	<br>
	<div class="row">
		<div class="col-md-6">
			<div id="tree-container"></div>
		</div>
		<div class="col-md-6">
			<div><strong>Ontology Term Information</strong></div></br>
			<div id="ontology-term-info"></div>
		</div>
	</div>
	<#else>
	<div class="row">
		There are not ontologies available!
	</div>
	</#if>
</form>
<script type="text/javascript">
	$(document).ready(function(){
		var molgenis = window.top.molgenis;
		var ontologyTree = new molgenis.OntologyTree('tree-container');
		ontologyTree.updateOntologyTree($('#selectOntologies').val());
		$('#selectOntologies').change(function(){
			ontologyTree.updateOntologyTree($(this).val());
		});
		$('#searchButton').click(function(event){
			event.preventDefault();
			ontologyTree.queryTree($('#selectOntologies').val(), $('#searchField').val());
		});
		$('#clearButton').click(function(event){
			event.preventDefault();
			$('#searchField').val('');
			ontologyTree.restoreTree();
		});
	});
</script>
<@footer/>	