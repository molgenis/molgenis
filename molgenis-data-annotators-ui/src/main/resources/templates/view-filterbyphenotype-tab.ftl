<#macro phenotypefilter_panel>
	<#--Panel 3: Phenotype selection-->
	<div class="tab-pane" id="tab3">
		<form class="form" role="form">
			<h6>Select a phenotype database</h6>
	
			<select name="" class="form-control">
			    <option value="phenotype-database-hpo">HPO database</option>
			    <option value="phenotype-database-cgd">CGD database</option>
				<option value="phenotype-database-omim">OMIM database</option>
			</select>
	
			<h6>Select a phenotype</h6>

			<select class="phenotypeSelect" data-placeholder="Make a selection.." multiple class="chosen">
				<option value="#">Disease Y</option>
				<option value="#">Disease X</option>
			</select>
	
			<button type="submit" class="btn">Add</button>
			
		</form>

		<hr></hr>

		<h5>Selected phenotypes</h5>
		<h7>No phenotypes selected</h7>
		
	</div>
</#macro>