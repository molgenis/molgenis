<#macro filter_by_genomic_region_tab>
	<#--Panel 2: Input gene panels or genomic locations-->
	<div class="tab-pane" id="tab2">
		<div class="checkbox">
			<label>
				<input type="checkbox"> Onco Panel
				<span id="help-icon-hover" href="#" data-placement="auto" 
					data-toggle="tooltip" 
					title="Panel containing transcript regions from known onco diagnostic genes. Ensemble build 73, GRCh37.12" 
					class="icon-question-sign">
				</span>
			</label>
		</div>
		
		<div class="checkbox">
			<label>
				<input type="checkbox"> Cardiac Panel
				<span id="help-icon-hover" href="#" data-placement="auto" 
					data-toggle="tooltip" 
					title="Panel containing transcript regions from known cardiac diagnostic genes. Ensemble build 73, GRCh37.12" 
					class="icon-question-sign">
				</span>
			</label>
		</div>
		
		<div class="checkbox">
			<label>
				<input type="checkbox"> Preconception Panel
				<span id="help-icon-hover" href="#" data-placement="auto" 
					data-toggle="tooltip" 
					title="Panel containing transcript regions from known preconception diagnostic genes. Ensemble build 73, GRCh37.12" 
					class="icon-question-sign">
				</span>
			</label>
		</div>
	</div>
</#macro>