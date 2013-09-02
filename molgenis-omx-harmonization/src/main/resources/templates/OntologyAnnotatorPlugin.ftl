<!DOCTYPE html>
<html>
	<head>
		<title>Data indexer plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap-datetimepicker.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap-fileupload.min.css" type="text/css">
		<link rel="stylesheet" href="/css/ontology-annotator.css" type="text/css">
        <#if app_href_css??>
            <link rel="stylesheet" href="${app_href_css}" type="text/css">
        </#if>
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/common-component.js"></script>
		<script type="text/javascript" src="/js/ontology-annotator.js"></script>
		<script type="text/javascript">
			$(document).ready(function(){
				var molgenis = window.top.molgenis;
				$('#selectedDataSet').change(function(){
					molgenis.changeDataSet($(this).val());
				});
				molgenis.changeDataSet($('#selectedDataSet').val());
			});
		</script>
	</head>
	<body>
		<div id="container-div" class="container-fluid">
			<div class="row-fluid">
				<div class="span12">
					<div id="alert-message"></div>
					<div>
						<h1>Harmonization</h1>
					</div>
					<div>
						<select id="selectedDataSet" name="selectedDataSet">
							<#list dataSets as dataset>
									<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
							</#list>
						</select>
					</div>
					<div class="row-fluid">
						<div class="span8">
							<div class="row-fluid">
								<div class="input-append span4">
									<input id="search-text" type="text" title="Enter your search term" />
									<button class="btn" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
								</div>
							</div>
							<div class="row-fluid data-table-container">
								<table id="dataitem-table" class="table table-striped table-condensed">
								</table>
								<div class="pagination pagination-centered">
									<ul></ul>
								</div>
								<div>
									<button id="annotate-dataitems" class="btn">Go annotation</button>
									<button id="refresh-button" class="btn">Refresh</button>
									<button id="match-catalogue" class="btn">Match catalogue</button>
								</div>
							</div>
						</div>
						<div class="span4">
							<div class="accordion-group">
							    <div class="accordion-heading">
									<h5 class="text-left text-info">Help!</h5>	
								</div>
								<div class="accordion-body in">
									<p class="justify-text">Ontologies are used to expand semantics of data items so that those data items annotated with 
									ontologies terms would have more synonyms and relevent terms that would be more likely matched with 
									other catalogue data items. Please index the ontologies in advance. It will largely speed up the matching process.
									</p>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>