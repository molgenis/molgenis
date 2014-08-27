<script src="<@resource_href "/js/jquery-ui-1.9.2.custom.min.js"/>"></script>
<script src="<@resource_href "/js/jquery.bootstrap.pager.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-fileupload.min.js"/>"></script>
<script src="<@resource_href "/js/common-component.js"/>"></script>
<script src="<@resource_href "/js/biobank-connect.js"/>"></script>
<script src="<@resource_href "/js/ontology-matcher.js"/>"></script>
<link rel="stylesheet" href="<@resource_href "/css/jquery-ui-1.9.2.custom.min.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/biobank-connect.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/ontology-matcher.css"/>" type="text/css">
<form id="wizardForm" name="wizardForm" method="post" class="form-horizontal">
	<div class="row">
		<div class="col-md-12">
			<div class="row">
				<div class="col-md-offset-3 col-md-6 div-inputinfo well">
					<div class="row">
						<div class="col-md-12">
							<legend class="ontology-matcher-legend">
								Select matching catalogues for : <strong>${wizard.selectedDataSet.name}</strong>
							</legend>
							<div class="row">
								<div class="col-md-4">
									<select id="targetDataSets" name="targetDataSets">
										<#list wizard.dataSets as dataset>
											<#if dataset.id?c != wizard.selectedDataSet.id?c>
												<option value="${dataset.id?c}"<#if dataset.id?c == wizard.selectedDataSet.id?c> selected</#if>>${dataset.name}</option>
											</#if>
										</#list>
									</select>
									<input type="hidden" id="selectedTargetDataSets" name="selectedTargetDataSets">
								</div>
								<div class="btn-group col-md-offset-2 col-md-3">
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
				<div id="div-info" class="col-md-12 well">	
					<div class="row">
						<div class="span9"><legend class="legend">Browse catalogue : <strong><span id="selected-catalogue"></span></strong></legend></div>
						<div  id="div-search" class="col-md-3">
							<div><strong>Search data items :</strong></div>
							<div class="group-append">
								<input id="search-dataitem" type="text" title="Enter your search term" />
								<button class="btn btn-default" type="button" id="search-button"><i class="icon-large icon-search"></i></button>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-md-12">
							<div id="container" class="data-table-container"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</form>