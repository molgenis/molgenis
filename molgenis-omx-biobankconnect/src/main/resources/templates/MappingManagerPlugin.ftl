<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[
	"jquery-ui-1.9.2.custom.min.css", 
	"bootstrap-fileupload.min.css", 
	"biobank-connect.css"]>
<#assign js=[
	"jquery-ui-1.9.2.custom.min.js", 
	"jquery.bootstrap.pager.js",
	"bootstrap-fileupload.min.js", 
	"common-component.js", 
	"ontology-annotator.js", 
	"mapping-manager.js",
	"biobank-connect.js", 
	"simple_statistics.js",
	"d3.min.js",
	"vega.min.js",
	"jstat.min.js",
	"biobankconnect-graph.js"]>
<@header css js/>
<script src="<@resource_href "/js/ace-min/ace.js"/>" type="text/javascript" charset="utf-8"></script>
<script src="<@resource_href "/js/ace-min/ext-language_tools.js"/>" type="text/javascript" charset="utf-8"></script>
<form id="wizardForm" class="form-horizontal" enctype="multipart/form-data">
	<div class="row">
		<div class="col-md-12 well custom-white-well">
			<br>
			<div class="row">
				<div class="col-md-offset-3 col-md-6 text-align-center">
					<legend class="custom-purple-legend">Curate matches &nbsp;
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
					</legend>
				</div>
			</div>
			<div class="row">
				<div  id="div-search" class="col-md-12">
					<div><strong>Search data items :</strong></div>
					<div class="group-append row">
						<div class="col-md-3">
							<input id="search-dataitem" type="text" title="Enter your search term" />
							<button class="btn btn-default" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
						</div>
						<div class="col-md-1">
							<button id="downloadButton" class="btn btn-primary">Download</button>
						</div>
						<div class="col-md-offset-7 col-md-1">
							<a id="help-button" class="btn btn-default">help <i class="icon-question-sign icon-large"></i></a>
						</div>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-4">
					Number of data items : <span id="dataitem-number"></span>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">
					<div id="container" class="row data-table-container">
					</div>
				</div>
			</div>
		</div>
	</div>
</form>
	<script type="text/javascript">
		$(document).ready(function(){
			var molgenis = window.top.molgenis;
			var contextUrl = '${context_url}';
			contextUrl = contextUrl.replace('/mappingmanager', '/biobankconnect');
			molgenis.setContextUrl(contextUrl);
			molgenis.ontologyMatcherRunning(function(){
				var mappingManager = new molgenis.MappingManager();
				var mappedDataSetIds = mappingManager.getAllMappedDataSetIds('${userName}', $('#selectedDataSet').val());
				mappingManager.changeDataSet('${userName}', $('#selectedDataSet').val(), mappedDataSetIds);
				$('#selectedDataSet').change(function(){
					var mappedDataSetIds = mappingManager.getAllMappedDataSetIds('${userName}', $('#selectedDataSet').val());
					mappingManager.changeDataSet('${userName}', $(this).val(), mappedDataSetIds);
				});
				$('#downloadButton').click(function(){
					mappingManager.downloadMappings();
					return false;
				});
				$('#help-button').click(function(){
					mappingManager.createHelpModal();
				});
				
			}, contextUrl);
		});
	</script>
<@footer/>	