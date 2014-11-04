<script src="<@resource_href "/js/jquery.bootstrap.pager.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-fileupload.min.js"/>"></script>
<script src="<@resource_href "/js/common-component.js"/>"></script>
<script src="<@resource_href "/js/biobank-connect.js"/>"></script>
<script src="<@resource_href "/js/catalogue-chooser.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-select/bootstrap-select.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-typeahead/typeahead.bundle.min.js"/>"></script>
<link rel="stylesheet" href="<@resource_href "/css/jquery-ui-1.9.2.custom.min.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/bootstrap-fileupload.min.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/biobank-connect.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/bootstrap-select/bootstrap-select.min.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/catalogue-chooser.css"/>" type="text/css">
<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal" enctype="multipart/form-data">
	<div id="div-info" class="col-md-12 well custom-white-well">	
		<div class="row">
			<div class="col-md-offset-3 col-md-6 text-align-center">
				<legend class="custom-purple-legend">
					Browse catalogue &nbsp;
					<select id="selectedDataSetId" name="selectedDataSetId" class="selectpicker" data-style="btn-primary">
						<#if wizard.selectedDataSet??>
							<#list wizard.dataSets as dataset>
								<option value="${dataset.id?c}"<#if dataset.id?c == wizard.selectedDataSet.id?c> selected</#if>>${dataset.name}</option>
							</#list>
						<#else>
							<#list wizard.dataSets as dataset>
								<option value="${dataset.id?c}"<#if dataset_index == 0> selected</#if>>${dataset.name}</option>
							</#list>
						</#if>
					</select>
				</legend>
			</div>
			<div class="col-md-3"><button id="import-data-button" class="btn btn-primary float-right" type="btn">Import data</button></div>
		</div>
		<div class="row">
			<div id="div-search" class="col-md-3">
				<div><strong>Search data items :</strong></div>
				<div class="input-group">
					<input id="search-dataitem" class="form-control" title="Enter your search term" style="border-top-left-radius:5px;border-bottom-left-radius:5px;"/>
					<div id="search-button" class="input-group-addon"><span class="glyphicon glyphicon-search"></span></div>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-md-4">
				Number of data items : <span id="dataitem-number"></span>
			</div>
		</div><br>
		<div class="row">
			<div class="col-md-12">
				<div id="container" class="data-table-container"></div>
			</div>
		</div>
	</div>
</div>
	<script type="text/javascript">
		$(function(){
			var molgenis = window.top.molgenis;
			molgenis.ontologyMatcherRunning(function(){
				var catalogueChooser = new molgenis.CatalogueChooser();
				catalogueChooser.changeDataSet($('#selectedDataSetId').val());
				
				$('#selectedDataSetId').change(function(){
					catalogueChooser.changeDataSet($('#selectedDataSetId').val());
				});
				
				if($('#selectedDataSetId option').length === 0){
					$('.pager li.next').addClass('disabled');
				}
				
				$('#import-data-button').click(function(){
					$('#import-features-modal').modal('show');
					return false;
				});
				
				$('#import-features').click(function(){
					var alert = {};
					var uploadedFile = $('#uploadedOntology').val();
					if($('#dataSetName').val() === ''){
						alert.message = 'Please define the dataset name!';
						molgenis.createAlert([alert], 'error');
						$('#import-features-modal').modal('hide');
					}else if(uploadedFile === '' || uploadedFile.substr(uploadedFile.length - 4, uploadedFile.length) !== '.csv'){
						alert.message = 'Please upload your file in CSV';
						molgenis.createAlert([alert], 'error');
						$('#import-features-modal').modal('hide');
					}else{
						$('#wizardForm').attr({
							'action' : '${context_url}/uploadfeatures',
							'method' : 'POST'
						}).submit();
					}
				});
				<#if message??>
					var alert = {};
					alert.message = '${message}';
					molgenis.createAlert([alert], 'error');
				</#if>
			});
		});
	</script>
</form>