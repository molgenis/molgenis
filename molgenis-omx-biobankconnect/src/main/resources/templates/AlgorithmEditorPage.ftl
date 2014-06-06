<script src="/js/jquery-ui-1.9.2.custom.min.js"></script>
<script src="/js/jquery.bootstrap.pager.js"></script>
<script src="/js/bootstrap-fileupload.min.js"></script>
<script src="/js/common-component.js"></script>
<script src="/js/biobank-connect.js"></script>
<script src="/js/algorithm-editor.js"></script>
<script src="/js/jstat.min.js"></script>
<script src="/js/d3.min.js"></script>
<script src="/js/vega.min.js"></script>
<script src="/js/biobankconnect-graph.js"></script>
<script src="/js/ace-min/ace.js" type="text/javascript" charset="utf-8"></script>
	<script src="/js/ace-min/ext-language_tools.js" type="text/javascript" charset="utf-8"></script>
<link rel="stylesheet" type="text/css" href="/css/jquery-ui-1.9.2.custom.min.css">
<link rel="stylesheet" type="text/css" href="/css/biobank-connect.css">
<link rel="stylesheet" type="text/css" href="/css/algorithm-editor.css">
<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal">
	<div class="row-fluid">
		<div id="div-info" class="span12 well custom-white-well">	
			<div class="row-fluid">
				<div class="offset3 span6 text-align-center">
					<legend class="custom-purple-legend">Harmonize dataset &nbsp;<strong>${wizard.selectedDataSet.name}</strong></legend>
				</div>
			</div>
			<div class="row-fluid">
				<div  id="div-search" class="span3">
					<div><strong>Search data items :</strong></div>
					<div class="input-append">
						<input id="search-dataitem" type="text" title="Enter your search term" />
						<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span4">
					Number of data items : <span id="dataitem-number"></span>
				</div>
			</div>
			<div class="row-fluid">
				<div class="span12">
					<div id="container"></div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(function(){
			var dataSetIds = [];
			<#list wizard.selectedBiobanks as dataSetId>
				dataSetIds.push('${dataSetId?c}');
			</#list>
			var molgenis = window.top.molgenis;
			var algorithmEditor = new molgenis.AlgorithmEditor();
			algorithmEditor.changeDataSet('${wizard.userName}', '${wizard.selectedDataSet.id?c}', dataSetIds);
			$('#selectedDataSetId').change(function(){
				algorithmEditor.changeDataSet('${wizard.userName}', '${wizard.selectedDataSet.id?c}', dataSetIds);
			});
		});
	</script>
</form>