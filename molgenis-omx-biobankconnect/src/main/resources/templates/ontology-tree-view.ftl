<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "bootstrap-fileupload.min.css", "ui.fancytree.min.css", "biobank-connect.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "jquery.fancytree.min.js", "jquery.molgenis.tree.js", "ontology-tree.js"]>
<@header css js/>
<form id="evaluationForm" class="form-horizontal" enctype="multipart/form-data">
	<div class="row-fluid">
		<legend>
			<h3><center>Ontology View</center></h3>
		</legend>
	</div>
	<#if ontologies?? && (ontologies?size > 0)>
		<div class="row-fluid">
			<div class="span6">
				<div class="btn-group">
				<input id="searchField" type="text">
				<button id="searchButton" type="text" class="btn btn-primary">Search</button>
				</div>
			</div>
			<div class="offset3 float-right">
				<select id="selectOntologies">
					<#list ontologies as ontology>
						<option value="${ontology.identifier}">${ontology.name}</option>
					</#list>
				</select>
			</div>
		</div>
		<br>
		<div class="row-fluid">
			<div class="span6">
				<div id="tree-container"></div>
			</div>
			<div class="span6">
				<div><strong>Ontology Term Information</strong></div></br>
				<div id="ontology-term-info"></div>
			</div>
		</div>
	<#else>
		<div class="row-fluid">
			There are not ontologies available!
		</div>
	</#if>
</form>
<script type="text/javascript">
	$(document).ready(function(){
		var molgenis = window.top.molgenis;
		var ontologyTree = new molgenis.OntologyTree();
		ontologyTree.updateOntologyTree($('#selectOntologies').val());
		$('#selectOntologies').change(function(){
			ontologyTree.updateOntologyTree($(this).val());
		});
		$('#searchButton').click(function(event){
			event.preventDefault();
			ontologyTree.queryTree($('#selectOntologies').val(), $('#searchField').val());
		});
	});
</script>
<@footer/>	