<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "bootstrap-fileupload.min.css", "ui.fancytree.min.css", "biobank-connect.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "bootstrap-fileupload.min.js", "jquery.fancytree.min.js", "jquery.molgenis.tree.js", "simple_statistics.js"]>
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
				<select id="selectOntologies">
					<#list ontologies as ontology>
						<option value="${ontology.identifier}">${ontology.name}</option>
					</#list>
				</select>
			</div>
		</div>
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
	function createEntityMetaTree(entityMetaData, attributes) {
		var container = $('#tree-container').css({
			'height' : '500px',
			'overflow' : 'auto'
		});
		container.tree({
			'entityMetaData' : entityMetaData,
			'selectedAttributes' : attributes,
			'onAttributesSelect' : function(selects) {
				console.log(selects);
			},
			'onAttributeClick' : function(attribute) {
				var request = {
					'ontologyUrl' : attribute.ontologyUrl,
					'ontologyTermUrl' : attribute.name
				};
				$.ajax({
					type : 'POST',
					url : molgenis.getContextUrl() + '/info',
					async : false,
					data : JSON.stringify(request),
					contentType : 'application/json',
					success : function(data, textStatus, request) {
						ontologyTermInfo(data);
					},
					error : function(request, textStatus, error){
						console.log(error);
					} 
				});
			},
			'lazyload' : function(data, createChildren, doSelect){
				var request = {
					'ontologyUrl' : data.node.data.attribute.ontologyUrl,
					'nodePath' : data.node.data.attribute.nodePath,
					'ontologyTermUrl' : data.node.data.attribute.name
				};
				var attributes = [];
				$.ajax({
					type : 'POST',
					url : molgenis.getContextUrl() + '/meta',
					async : false,
					data : JSON.stringify(request),
					contentType : 'application/json',
					success : function(data, textStatus, request) {
						$.each(data.attributes, function(key, attribute) {
							attributes.push(attribute);
						});
					},
					error : function(request, textStatus, error){
						console.log(error);
					} 
				});
				data.result = createChildren(attributes, doSelect);
			}
		});
	}
	
	function ontologyTermInfo(data){
		var table = $('<table />').addClass('table');
		table.append('<tr><th>Ontology</th><td><a href="' + data.name + '" target="_blank">' + data.ontologyUrl + '</a></td></tr>');
		table.append('<tr><th>OntologyTerm</th><td><a href="' + data.name + '" target="_blank">' + data.name + '</a></td></tr>');
		table.append('<tr><th>Name</th><td>' + data.label + '</td></tr>');
		if(data.definition){
			table.append('<tr><th>Definition</th><td>' + data.definition + '</td></tr>');
		}
		if(data.synonyms && data.synonyms.length > 0){
			var listOfSynonyms = $('<ul />');
			$.each(data.synonyms, function(index, synonym){
				listOfSynonyms.append('<li>' + synonym + '</li>');
			});
			var synonymContainer = $('<td />').append(listOfSynonyms);
			$('<tr />').append('<th>Synonyms</th>').append(synonymContainer).appendTo(table);
		}
		table.find('th').width('30%');
		$('#ontology-term-info').empty().append(table);
	}
	
	function updateOntologyTree(ontologyUrl){
		var molgenis = window.top.molgenis;
		var request = {
			'ontologyUrl' : ontologyUrl
		}; 
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/build',
			async : false,
			data : JSON.stringify(request),
			contentType : 'application/json',
			success : function(data, textStatus, request) {
				createEntityMetaTree(data);
			},
			error : function(request, textStatus, error){
				console.log(error);
			} 
		});
	}
	$(document).ready(function(){
		updateOntologyTree($('#selectOntologies').val());
		$('#selectOntologies').change(function(){
			updateOntologyTree($(this).val());
		});
	});
</script>
<@footer/>	