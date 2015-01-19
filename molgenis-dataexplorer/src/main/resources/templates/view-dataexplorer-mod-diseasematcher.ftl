<#include "resource-macros.ftl">
<!--DISEASE MATCHER / PHENOVIEWER -->
<div class="row">	
	<div class="col-md-12" id="disease-matcher">
	
		<#-- ANALYSIS ZONE -->
		<div class="col-md-9">
			<div class="row well" id="diseasematcher-infopanel">
				<div class="col-md-12" id="diseasematcher-variant-panel"></div>
				
				<div class="row">
					<div class="col-md-12" id="diseasematcher-disease-panel">
						<div class="top-buffer"/>
						<ul class="nav nav-tabs" id="diseasematcher-disease-panel-tabs" data-tabs="tabs"></ul>
						<div id="diseasematcher-disease-tab-content"></div>
					</div>
				</div>
			</div>
			<div class="well" id="diseasematcher-patientpanel" style="display: none;">
				<div class="row filter-menu">
					<div class="row">
						<div class="col-md-12"><h3 class="text-center">PhenoTips disease filter</h3></div>
					</div>
					
					<div class="top-buffer"/>
					
					<div class="col-md-6">
						<label for="hpoTerms">HPO terms:</label><br/>
						<textarea id="hpoTermsInput" name="hpoTerms" rows="3" cols="40" rows="4">HP:0000252,HP:0004322,HP:0009900,HP:0001263,HP:0007369,HP:0001272,HP:0002011</textarea>		
					</div>
					<div class="col-md-6 center-aligned text-center">
							<button type="button" class="btn btn-primary" id="btn-filter-phenotips-complete">Match PhenoTips Predictions</button>
					</div>
				</div>
			
				<div class="top-buffer"/>
			
				<div class="row" id="diseasematcher-filter-output" style="display: none;">
					<div class="panel-group" id="filter-output-collapse">
					
						<#-- FILTERED DATA PANEL -->
						<div class="panel">
							<div class="panel-heading">
								<h4 class="panel-title">
									<a data-toggle="collapse" data-parent="#filter-output-collapse" data-target="#collapseData" href="#collapseData">Filtered data</a>
								</h4>
							</div>
							<div id="collapseData" class="panel-collapse collapse">
								<div class="panel-body">
									<div class="row" >
										<div class="col-md-12" id="diseasematcher-filtered-variants"></div>
									</div>
									<div class="row">
										<div class="col-m2-12">
											<button id="diseasematcher-download-button" class="btn btn-primary pull-right" style="display: none;">Download</button>
										</div>
									</div>
								</div>
							</div>
						</div>
							
						<#--PERFECT MATCHES PANEL -->
						<div class="panel">
							<div class="panel-heading">
								<h4 class="panel-title">
									<a id="perfectMatchTitle" data-toggle="collapse" data-parent="#filter-output-collapse" data-target="#collapsePerfectMatches" href="#collapsePerfectMatches">Perfect matches</a>
								</h4>
							</div>
							<div id="collapsePerfectMatches" class="panel-collapse collapse">
								<div class="panel-body">
									<div id="diseasematcher-filter-perfect"></div>
								</div>
							</div>
						</div>	
						
						<#-- SIMILAR MATCHES PANEL -->
						<div class="panel">
							<div class="panel-heading">
								<h4 class="panel-title">
									<a id="similarMatchTitle" data-toggle="collapse" data-parent="#filter-output-collapse" data-target="#collapseSimilarMatches" href="#collapseSimilarMatches">Similar matches</a>
								</h4>
							</div>
							<div id="collapseSimilarMatches" class="panel-collapse collapse">
								<div class="panel-body">
									<div id="diseasematcher-filter-similar"></div>
								</div>
							</div>
						</div>	
						
						<#-- NO MATCHES PANEL-->
						<div class="panel">
							<div class="panel-heading">
								<h4 class="panel-title">
									<a id="noMatchTitle" data-toggle="collapse" data-parent="#filter-output-collapse" data-target="#collapseNoMatches" href="#collapseNoMatches">No matches</a>
								</h4>
							</div>
							<div id="collapseNoMatches" class="panel-collapse collapse">
								<div class="panel-body">
									<div id="diseasematcher-filter-no"></div>
								</div>
							</div>
						</div>	
					</div>
				</div>
				
				
				<div id="diseasematcher-phenotips-hiddenoutput"></div>

			</div>
		</div>
		
		<#-- DISEASE ZONE-->
		<div class="col-md-3">
			<div class="well">
				<nav class="navbar navbar-default">
					<div class="navbar-header">
						<ul class="nav navbar-nav" id="diseasematcher-selection-navbar-nav">
							<li><a href="#" id="diseasematcher-genes-select-button">Genes</a></li>
							<li><a href="#" id="diseasematcher-diseases-select-button">Diseases</a></li>
						
							<li><a href="#" id="diseasematcher-patient-select-button">Filter</a></li>
						</ul>
					</div>
				</nav>
		
				<div class="row"></div>
				
				<div class="row">
					<div class="col-md-12">
						<div class="panel" id="disease-selection-container">
                            <div class="panel-heading">
                                <h4 class="panel-title" id="diseasematcher-selection-title"></h4>
                            </div>
                    
                            <div class="panel-body">
                                
                                <div class="row" id="disease-selection">
                                	<div class="col-md-12">
                                		<ul class="nav nav-pills nav-stacked" id="diseasematcher-selection-list"></ul>
                                		<div class="pagination pagination-centered" id="diseasematcher-selection-pager"></div>
                                	</div>
                                </div>
                                
                            </div>
	                    </div>
	            	</div>
            	</div>
			</div>
		</div>
	</div>		
</div>

<script id="hb-dataset-warning" class="diseasematcher" type="text/x-handlebars-template">
	<div class="alert alert-warning" id="{{dataset}}-warning">
		<strong>{{dataset}} not loaded!</strong> For this tool to work, please upload a valid <em>{{dataset}}</em> dataset.
	</div>	
</script>

<script id="hb-column-warning" class="diseasematcher" type="text/x-handlebars-template">
	<div class="alert alert-warning" id="{{column}}-warning">
		<strong>No {{column}} column found!</strong> For this tool to work, make sure your dataset has a <em>{{column}}</em> column.
	</div>	
</script>

<script id="hb-selection-list" class="diseasematcher" type="text/x-handlebars-template">
	{{#if this.0.diseaseId}}
		{{#each this}}
			<li><a href="#" class="diseasematcher-disease-listitem" id="{{diseaseId}}">
				{{#if diseaseName}}
					{{diseaseName}}
				{{else}}
					{{diseaseId}}
				{{/if}}	
			</a></li>
		{{else}}
			<p>No diseases found...</p>
		{{/each}}
	{{else}}
		{{#each this}}
			<li><a href="#" class="diseasematcher-disease-listitem" id="{{this}}">
				{{this}}
			</a></li>
		{{else}}
			<p>No genes found...</p>
		{{/each}}
	{{/if}}
</script>

<script>
	var tableEditable = ${tableEditable?string('true', 'false')};
	$.when($.ajax("<@resource_href "/js/dataexplorer-diseasematcher.js"/>", {'cache': true}))
			.then(function() {
	});
</script>