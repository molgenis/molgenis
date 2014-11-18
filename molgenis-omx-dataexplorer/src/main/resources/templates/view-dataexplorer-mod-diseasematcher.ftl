<#include "resource-macros.ftl">
<!--DISEASE MATCHER / PHENOVIEWER -->
<div class="row">	
	<div class="col-md-12" id="disease-matcher">
	
		<#-- ANALYSIS ZONE -->

		<div class="col-md-9">
			<div class="well" id="diseasematcher-infopanel">
				<div class="span12" id="diseasematcher-variant-panel"></div>
				<div class="row-fluid">
					<div class="span12" id="diseasematcher-disease-panel">
						<ul class="nav nav-tabs" id="diseasematcher-disease-panel-tabs" data-tabs="tabs"></ul>
						<div id="diseasematcher-disease-tab-content"></div>
					</div>
				</div>
			</div>
			<div class="well" id="diseasematcher-patientpanel" style="display: none;">
				<div class="well" id ="diseasematcher-filtered-variants"></div>
				<button id="diseasematcher-download-button" class="btn btn-primary" style="display: none;">Download</button>
				<legend>Phenotips</legend>
				<label for="hpoTerms">HPO terms:</label>
				<textarea id="hpoTermsInput" name="hpoTerms" rows="3" cols="30">HP%3A0000252,HP%3A0004322,HP%3A0009900,HP%3A0001263,HP%3A0007369,HP%3A0001272,HP%3A0002011</textarea>
				<br/>
				<button type="button" id="btn-filter-phenotips-complete">Match PhenoTips Predictions</button>

				<div id="diseasematcher-phenotips-output"></div>
				<div id="diseasematcher-phenotips-hiddenoutput"></div>

			</div>
		</div>
		
		<#-- DISEASE ZONE-->
		<div class="col-md-3">
			<div class="well">
				<div class="navbar-nav" id="diseasematcher-selection-navbar-nav">
					<div class="navbar-nav-inner">
						<ul class="nav">
							<li><a href="#" id="diseasematcher-genes-select-button">Genes</a></li>
							<li><a href="#" id="diseasematcher-diseases-select-button">Diseases</a></li>
						</ul>
						<ul class="nav pull-right">
							<li><a href="#" id="diseasematcher-patient-select-button">Patient</a></li>
						</ul>
					</div>
				</div>
		
				<div class="row"></div>
				
				<div class="row">
					<div class="accordion" id="disease-selection-container">
                        <div class="accordion-group">
                            <div class="accordion-heading">
                                <a class="accordion-toggle" data-toggle="false" data-parent="#disease-selection-container" href="#disease-selection" id="diseasematcher-selection-title"></a>
                            </div>
                    
                            <div class="accordion-body collapse in">
                                <div class="accordion-inner">
                                    <div class="row" id="disease-selection">
                                    	
                                    	<ul class="nav nav-tabs nav-stacked" id="diseasematcher-selection-list"></ul>
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