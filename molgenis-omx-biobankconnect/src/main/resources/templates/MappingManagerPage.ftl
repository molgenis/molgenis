<script src="<@resource_href "/js/jquery-ui-1.9.2.custom.min.js"/>"></script>
<script src="<@resource_href "/js/jquery.bootstrap.pager.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-fileupload.min.js"/>"></script>
<script src="<@resource_href "/js/common-component.js"/>"></script>
<script src="<@resource_href "/js/ontology-annotator.js"/>"></script>
<script src="<@resource_href "/js/mapping-manager.js"/>"></script>
<script src="<@resource_href "/js/biobank-connect.js"/>"></script>
<script src="<@resource_href "/js/simple_statistics.js"/>"></script>
<script src="<@resource_href "/js/d3.min.js"/>"></script>
<script src="<@resource_href "/js/vega.min.js"/>"></script>
<script src="<@resource_href "/js/jstat.min.js"/>"></script>
<script src="<@resource_href "/js/biobankconnect-graph.js"/>"></script>
<script src="<@resource_href "/js/ace-min/ace.js"/>" type="text/javascript" charset="utf-8"></script>
<script src="<@resource_href "/js/ace-min/ext-language_tools.js"/>" type="text/javascript" charset="utf-8"></script>
<link rel="stylesheet" href="<@resource_href "/css/jquery-ui-1.9.2.custom.min.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/biobank-connect.css"/>" type="text/css">
<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal">
	<div class="row">
		<div class="col-md-12 well custom-white-well">	
			<div class="row">
				<div class="col-md-offset-3 col-md-6 text-align-center">
					<legend class="custom-purple-legend">Curate matches &nbsp;<strong>${wizard.selectedDataSet.name}</strong></legend>
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
					<div id="container" class="row data-table-container"></div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function(){
			var dataSetIds = [];
			<#list wizard.selectedBiobanks as dataSetId>
				dataSetIds.push('${dataSetId?c}');
			</#list>
			var molgenis = window.top.molgenis;
			var mappingManager = new molgenis.MappingManager();
			mappingManager.changeDataSet('${wizard.userName}', '${wizard.selectedDataSet.id?c}', dataSetIds);
			$('#downloadButton').click(function(){
				mappingManager.downloadMappings();
				return false;
			});
			$('#help-button').click(function(){
				mappingManager.createHelpModal();
			});
		});
	</script>
</form>