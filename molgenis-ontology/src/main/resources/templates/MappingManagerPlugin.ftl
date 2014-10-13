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
<script src="<@resource_href "/js/bootstrap-typeahead/typeahead.bundle.min.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-select/bootstrap-select.js"/>"></script>
<link rel="stylesheet" href="<@resource_href "/css/bootstrap-select/bootstrap-select.min.css"/>" type="text/css">
<form id="wizardForm" class="form-horizontal" enctype="multipart/form-data">
	<div class="row">
		<div class="col-md-12 well custom-white-well">
			<br>
			<div class="row">
				<div class="col-md-offset-3 col-md-6 text-align-center">
					<legend class="custom-purple-legend">Curate matches &nbsp;
						<select id="selectedDataSet" name="selectedDataSet" class="selectpicker">
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
			<div id="control-panel" class="row">
				<div class="col-md-12">
					<div><strong>Search data items :</strong></div>
					<div class="row">
						<div id="div-search" class="col-md-3">
							<div class="input-group">
								<input id="search-dataitem" class="form-control" title="Enter your search term" style="border-top-left-radius:5px;border-bottom-left-radius:5px;"/>
								<div id="search-button" class="input-group-addon"><span class="glyphicon glyphicon-search"></span></div>
							</div>
						</div>
						<div class="col-md-2">
							<button id="downloadButton" class="btn btn-primary">Download</button>
						</div>
						<div class="col-md-offset-5 col-md-2">
							<a id="help-button" class="btn btn-default float-right-align">Help  <span class="glyphicon glyphicon-question-sign glyphicon-large"></span></a>
						</div>
					</div>
					<div class="row">
						<div class="col-md-4">
							Number of data items : <span id="dataitem-number"></span>
						</div>
					</div><br>
				</div>
			</div>
			<div class="row">
				<div id="container" class="col-md-12"></div>
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