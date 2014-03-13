<#macro phenotypefilter_panel>
	<#--Panel 3: Phenotype selection-->
	<div class="tab-pane" id="tab3">
		<form class="form" role="form">
				
				
				<div>
					<div class="controls">
						<label class="control-label" for="phenotypeSelect">Select a phenotype database</label>
						<div class="dataset-select-position-container">
							<select id="phenotype-database-select" class="form-control">
							    <option value="phenotype-database-hpo">HPO database</option>
							    <option value="phenotype-database-cgd">CGD database</option>
								<option value="phenotype-database-omim">OMIM database</option>
							</select>
						</div>
					</div>
				</div>
				
				<br />
				
				<hr></hr>
					
			<div class="controls">
				<label class="control-label" for="phenotypeSelect">Select a phenotype to use as filter</label>
				<div class="dataset-select-position-container">
					<select id="phenotypeSelect" data-placeholder="Make a selection.." multiple class="chosen">
						<option value="#">Abnormality of the breast</option>
						<option value="#">Abnormal alpha granule content</option>
						<option value="#">Skin vesicle</option>
						<option value="#">Limited elbow flexion/extension</option>
						<option value="#">Cervical segmentation defects</option>
						<option value="#">Diaphyseal cortical sclerosis</option>
					</select>
				</div>
			</div>	
		</form>
	</div>
</#macro>