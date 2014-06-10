<!--DISEASE MATCHER / PHENOVIEWER -->
<div class="row-fluid">	
	<div class="span12" id="disease-matcher">
		
		<#-- PHENOTYPE ZONE
		
		<div class="span3">
			<div class="well">
				<div class="row-fluid">
					<div class="input-append span12" id="phenotype-search-container">
						<input class="span10" id="phenotype-search" type="text" placeholder="Search phenotypes">
						<button class="btn" type="button" id="phenotype-search-button"><i class="icon-large icon-search"></i></button>
					</div>					
				</div>
				<div class="row-fluid">
					<div class="accordion" id="phenotype-selection-container">
                        <div class="accordion-group">
                            <div class="accordion-heading">
                                <a class="accordion-toggle" data-toggle="false" data-parent="#phenotype-selection-container" href="#phenotype-selection">Phenotypes</a>
                            </div>
                            <div class="accordion-body collapse in">
                                <div class="accordion-inner">
                                    <div class="row-fluid" id="phenotype-selection"></div>
                                </div>
                            </div>
                        </div>
                    </div>
            	</div>
			</div>
		</div>
		-->
		

		<#-- ANALYSIS ZONE -->
		<div class="span9">
			<div class="well">
				<div class="span12" id="diseasematcher-variant-panel"></div>
				<div class="row-fluid">
					<div class="span12" id="diseasematcher-disease-panel">
						<ul class="nav nav-tabs" id="diseasematcher-disease-panel-tabs" data-tabs="tabs"></ul>
						<div id="diseasematcher-disease-tab-content"></div>
					</div>
				</div>
			</div>
		</div>
		

		<#-- DISEASE ZONE-->
		<div class="span3">
			<div class="well">
				<div class="navbar" id="diseasematcher-selection-navbar">
					<div class="navbar-inner">
						<ul class="nav">
							<li><a href="#" id="diseasematcher-diseases-select-button">Diseases</a></li>
							<li><a href="#" id="diseasematcher-genes-select-button">Genes</a></li>
						</ul>
					</div>
				</div>
		
				<div class="row-fluid">
					<#-- add span12 to ensure that input is styled correctly at low and high solutions -->
					<div class="input-append span12" id="disease-search-container">
						<#-- add span10 to ensure that input is styled correctly at low and high solutions -->
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
<script>
	$.when($.ajax("/js/dataexplorer-diseasematcher.js", {'cache': true}))
			.then(function() {
				//	
	});
</script>
