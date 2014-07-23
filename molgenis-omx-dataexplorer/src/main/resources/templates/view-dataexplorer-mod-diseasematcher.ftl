<!--DISEASE MATCHER / PHENOVIEWER -->
<div class="row-fluid">	
	<div class="span12" id="disease-matcher">
	
		<#-- ANALYSIS ZONE -->
		<div class="span9">
			<div class="well" id="diseasematcher-infopanel">		
			</div>
		</div>
		
		<#-- DISEASE ZONE-->
		<div class="span3">
			<div class="well">
				<div class="navbar" id="diseasematcher-selection-navbar">
					<div class="navbar-inner">
						<ul class="nav">
							<li><a href="#" id="diseasematcher-genes-select-button">Genes</a></li>
							<li><a href="#" id="diseasematcher-diseases-select-button">Diseases</a></li>
						</ul>
					</div>
				</div>
		
				<div class="row-fluid">
					<div class="input-append span12" id="disease-search-container">
						<input class="span10" id="diseasematcher-selection-search" type="text" placeholder="">
						<button class="btn" type="button" id="disease-search-button"><i class="icon-large icon-search"></i></button>
					</div>					
				</div>
				
				<div class="row-fluid">
					<div class="accordion" id="disease-selection-container">
                        <div class="accordion-group">
                            <div class="accordion-heading">
                                <a class="accordion-toggle" data-toggle="false" data-parent="#disease-selection-container" href="#disease-selection" id="diseasematcher-selection-title"></a>
                            </div>
                    
                            <div class="accordion-body collapse in">
                                <div class="accordion-inner">
                                    <div class="row-fluid" id="disease-selection">
                                    	
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

<script id="hb-layout-variant" class="diseasematcher" type="text/x-handlebars-template">
	<div class="span12" id="diseasematcher-variant-panel"></div>
	<div class="row-fluid">
		<div class="span12" id="diseasematcher-disease-panel">
			<ul class="nav nav-tabs" id="diseasematcher-disease-panel-tabs" data-tabs="tabs"></ul>
			<div id="diseasematcher-disease-tab-content"></div>
		</div>
	</div>
</script>

<script>
	var tableEditable = ${tableEditable?string('true', 'false')};
	$.when($.ajax("/js/dataexplorer-diseasematcher.js", {'cache': true}))
			.then(function() {
	});
</script>