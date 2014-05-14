<!--DISEASE MATCHER / PHENOVIEWER -->
<div class="row-fluid">	
	<div class="span12" id="disease-matcher">
		
		<!-- PHENOTYPE ZONE -->
		<div class="span3">
			<div class="well">
				<div class="row-fluid">
					<#-- add span12 to ensure that input is styled correctly at low and high solutions -->
					<div class="input-append span12" id="phenotype-search-container">
						<#-- add span10 to ensure that input is styled correctly at low and high solutions -->
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
		
		<!-- ANALYSIS ZONE -->
		<div class="span6">
			<div class="well">
				<div class="span12" id="vardump"></div>
				<div class="row-fluid">
					<div class="span6" id="diseasematcher-analysis-left"></div>
					<div class="span6" id="diseasematcher-analysis-right"></div>
				</div>
			</div>
		</div>
		
		<!-- DISEASE ZONE-->
		<div class="span3">
			<div class="well">
				<div class="disease-button-toolbar">
					<button type="button" class="btn btn-default btn-lg" id="grab-diseases-button">
						<span class="icon-large icon-arrow-down"></span> Grab diseases
					</button>
				</div>
				
				<div class="row-fluid">
					<#-- add span12 to ensure that input is styled correctly at low and high solutions -->
					<div class="input-append span12" id="disease-search-container">
						<#-- add span10 to ensure that input is styled correctly at low and high solutions -->
						<input class="span10" id="disease-search" type="text" placeholder="Search diseases">
						<button class="btn" type="button" id="disease-search-button"><i class="icon-large icon-search"></i></button>
					</div>					
				</div>
				
				<div class="row-fluid">
					<div class="accordion" id="disease-selection-container">
                        <div class="accordion-group">
                            <div class="accordion-heading">
                                <a class="accordion-toggle" data-toggle="false" data-parent="#disease-selection-container" href="#disease-selection">Diseases</a>
                            </div>
                    
                            <div class="accordion-body collapse in">
                                <div class="accordion-inner">
                                    <div class="row-fluid" id="disease-selection">
                                    	
                                    	<ul class="nav nav-tabs nav-stacked" id="disease-list"></ul>
                                    	<div class="pagination pagination-centered" id="disease-pager"></div>
                                    
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