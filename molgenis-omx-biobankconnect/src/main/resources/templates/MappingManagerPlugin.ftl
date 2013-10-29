<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["jquery-ui-1.9.2.custom.min.css", "biobank-connect.css", "mapping-manager.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "common-component.js", "catalogue-chooser.js", "ontology-annotator.js", "mapping-manager.js", "biobank-connect.js", "simple_statistics.js"]>
<@header css js/>
<form id="wizardForm" class="form-horizontal" enctype="multipart/form-data">
	<div class="row-fluid">
		<div class="span12 well">
			<div class="row-fluid">
				<div class="span12"><legend class="legend-mapping-manager">
					Curate mappings for :
					<select id="selectedDataSet" name="selectedDataSet">
						<#if selectedDataSet??>
							<#list dataSets as dataset>
								<option value="${dataset.id?c}"<#if dataset.id?c == selectedDataSet> selected</#if>>${dataset.name}</option>
							</#list>
						<#else>
							<#list dataSets as dataset>
								<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
							</#list>
						</#if>
					</select>
				</div>
			</div>
			<div class="row-fluid">
				<div  id="div-search" class="span12">
					<div><strong>Search data items :</strong></div>
					<div class="input-append row-fluid">
						<div class="span3">
							<input id="search-dataitem" type="text" title="Enter your search term" />
							<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
						</div>
						<div class="span1">
							<button id="downloadButton" class="btn btn-primary">Download</button>
						</div>
						<div class="offset7 span1">
							<a id="help-button" class="btn">help <i class="icon-question-sign icon-large"></i></a>
						</div>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span12">
					<div id="data-table-container" class="row-fluid data-table-container">
						<table id="dataitem-table" class="table table-striped table-condensed show-border">
						</table>
					</div>
					<div class="pagination pagination-centered">
						<ul id="table-papger"></ul>
					</div>
				</div>
			</div>
		</div>
	</div>
</form>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			molgenis.setContextURL('${context_url}');
			var dataSets = [];
			<#list dataSets as dataset>
				dataSets.push('${dataset.id?c}');
			</#list>
			molgenis.getMappingManager().changeDataSet($('#selectedDataSet').val(), dataSets);
			$('#selectedDataSet').change(function(){
				molgenis.getMappingManager().changeDataSet($(this).val(), dataSets);
			});
			$('#downloadButton').click(function(){
				molgenis.getMappingManager().downloadMappings();
				return false;
			});
			$('#help-button').click(function(){
				molgenis.getMappingManager().createHelpModal();
			});
		});
	</script>
<@footer/>	