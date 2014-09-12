<script src="<@resource_href "/js/jquery-ui-1.9.2.custom.min.js"/>"></script>
<script src="<@resource_href "/js/jquery.bootstrap.pager.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-fileupload.min.js"/>"></script>
<script src="<@resource_href "/js/common-component.js"/>"></script>
<script src="<@resource_href "/js/ontology-matcher.js"/>"></script>
<script src="<@resource_href "/js/biobank-connect.js"/>"></script>
<script src="<@resource_href "/js/select2.min.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-select/bootstrap-select.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-typeahead/typeahead.bundle.min.js"/>"></script>
<link rel="stylesheet" href="<@resource_href "/css/select2.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/jquery-ui-1.9.2.custom.min.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/biobank-connect.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/ontology-matcher.css"/>" type="text/css">
<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal">
	<div class="col-md-12 well custom-white-well">	
		<div class="row">
			<div class="col-md-offset-3 col-md-6 text-align-center">
				<legend class="custom-purple-legend">
					Select matching catalogues for <strong>${wizard.selectedDataSet.name}</strong>
				</legend>
			</div>
		</div>
		<div class="row">
			<div class="col-md-offset-3 col-md-6 div-inputinfo well">
				<div class="row">
					<div class="col-md-12">
						<div class="row">
							<div class="col-md-offset-1 col-md-4">
								<select id="targetDataSets" name="targetDataSets">
									<#list wizard.dataSets as dataset>
										<#if dataset.id?c != wizard.selectedDataSet.id?c>
											<option value="${dataset.id?c}">${dataset.name}</option>
										</#if>
									</#list>
								</select>
								<input type="hidden" id="selectedTargetDataSets" name="selectedTargetDataSets">
							</div>
							<div class="btn-group col-md-offset-2 col-md-4">
								<button id="add-target-dataset" class="btn btn-info" type="btn">Select</button>
								<button id="add-target-all-datasets" class="btn btn-primary" type="btn">Select all</button>
								<button id="remove-target-all-datasets" class="btn btn-default" type="btn">Remove all</button>
							</div>
						</div>
						<div id="target-catalogue" class="row"></div>
					</div>
				</div>
			</div>
		</div>
		<div id="catalogue-container" class="row">
			<div id="div-info" class="col-md-12 ">	
				<div class="row">
					<div id="div-search" class="col-md-3">
						<div><strong>Search data items :</strong></div>
						<div class="input-group">
							<input id="search-dataitem" class="form-control" title="Enter your search term" style="border-top-left-radius:5px;border-bottom-left-radius:5px;"/>
							<div id="search-button" class="input-group-addon"><span class="glyphicon glyphicon-search"></span></div>
						</div>
					</div>
				</div><br>
				<div class="row">
					<div class="col-md-12">
						<div id="container" class="data-table-container"></div>
					</div>
				</div>
			</div>
		</div>
	</div>
</form>
<script type="text/javascript">
	$(function(){
		var molgenis = window.top.molgenis;
		molgenis.ontologyMatcherRunning();
	});
</script>