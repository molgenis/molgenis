<!DOCTYPE html>
<html>
	<head>
		<title>Data indexer plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap-datetimepicker.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap-fileupload.min.css" type="text/css">
		<link rel="stylesheet" href="/css/ontology-matcher.css" type="text/css">
        <#if app_href_css??>
            <link rel="stylesheet" href="${app_href_css}" type="text/css">
        </#if>
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/common-component.js"></script>
		<script type="text/javascript" src="/js/bootstrap-fileupload.min.js"></script>
		<script type="text/javascript" src="/js/ontology-matcher.js"></script>
		<script type="text/javascript">
			$(document).ready(function(){
				var molgenis = window.top.molgenis;
				$('#protocol-id').change(function(){
					molgenis.changeDataSet($(this).val());
				});
				molgenis.changeDataSet($('#protocol-id').val());
				$('#downloadButton').click(function(){
					molgenis.downloadMappings();
					return false;
				});
			});
		</script>
	</head>
	<body>
		<div id="alertMessage">
		</div>
			<div class="container-fluid">
				<div>
					<h1>Matching result</h1>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<div class="row-fluid">
							<div class="span3">
								<select id="protocol-id" name="selectedDataSet">
									<#list dataSets as dataset>
											<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
									</#list>
								</select>
							</div>
							<div class="offset5 span4">
								<div class="accordion" id="feature-filters-container">
									<div class="accordion-group">
									    <div class="accordion-heading">
											<span class="accordion-toggle" data-toggle="false" data-parent="#feature-filters-container">Icon meanings</span>
										</div>
										<div class="accordion-body collapse in">
											<div class="accordion-inner" id="feature-filters">
												<div>
													<i class="icon-ok"></i>
													<span class="float-right text-success">Mappings have been selected</span>
												</div>
												<div>
													<i class="icon-pencil"></i>
													<span class="float-right text-info">Select the mappings</span>
												</div>
												<div>
													<i class="icon-trash"></i>
													<span class="float-right text-warning">Delete all mappings</span>
												</div>
												<div>
													<i class="icon-ban-circle"></i>
													<span class="float-right text-error">No candidate available</span>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<div class="row-fluid data-table-container">
									<table id="dataitem-table" class="table table-striped table-condensed show-border">
									</table>
									<div class="pagination pagination-centered">
										<ul></ul>
									</div>
								</div>
								<button id="downloadButton" class="btn">Download</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>