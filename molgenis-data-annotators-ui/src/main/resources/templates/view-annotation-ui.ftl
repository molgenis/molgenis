<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["annotate-ui.css", "jquery-ui-1.10.3.custom.css", "chosen.css", "bootstrap-fileupload.css"]>
<#assign js=["jquery-ui-1.10.3.custom.min.js", "chosen.jquery.min.js", "bootstrap-fileupload.js", "jquery.bootstrap.wizard.js"]>

<@header css js />
<@wizard />


<#macro wizard>
<div class="row-fluid">
	<div class="span12">
		<div id="rootwizard" class="tabbable tabs-left">
		
			<ul>
			  	<li><a href="#tab1" class="tab1" data-toggle="tab">Select or Upload a Dataset</a></li>
				<li><a href="#tab2" class="tab2" data-toggle="tab">Filter by: Genome</a></li>
				<li><a href="#tab3" class="tab3" data-toggle="tab">Filter by: Phenotype</a></li>
				<li><a href="#tab4" class="tab4" data-toggle="tab">Select your annotation</a></li>
		
				<li>
					<a><form role="form" action="${context_url}/execute-variant-app" method="post">
						<button type="submit" href="http://localhost:8080/menu/main/dataexplorer" 
							action="http://localhost:8080/menu/main/dataexplorer" class="btn">Go</button>
					</form></a>
				</li>
			</ul>
		
			<div class="tab-content" style="height:auto;">
				<#--panel code located in seperate macros for readability-->
		    	<@panel1 />
		    	<@panel2 />
		    	<@panel3 />
		    	<@panel4 />			
			</div>
		</div>
	</div>
</div>
</#macro>

<#macro panel1>
	<#--Panel 1: Input variants, manual or file(s)-->
	<div class="tab-pane" id="tab1">
		<div class="row-fluid">
			<div id="dataset-select-container">
				Choose a dataset:
				<div id="dataset-select-position-container" class="controls">
					<select name="dataset-select" id="dataset-select">
						<#list dataSets as dataSet>
							<option value="/api/v1/dataset/${dataSet.id?c}"<#if dataSet.identifier == selectedDataSet.identifier> selected</#if>>${dataSet.name}</option>
						</#list>
					</select>
				</div>
			</div>
		</div>	
			
		<hr></hr>
			
		<@modalPopupDataSetUpload />
		<a type="button" class="btn" href="#dataset-upload-modal" data-toggle="modal">Upload a new Data set</a>
		
	</div>
</#macro>

<#macro panel2>
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

<#macro panel3>
	<#--Panel 3: Phenotype selection-->
	<div class="tab-pane" id="tab3">
		<form class="form-horizontal" role="form" action="${context_url}/upload-phenotype-filter" method="post">
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

<#macro panel4>
	<#--Panel 4: Annotation tool / database selection-->
	<div class="tab-pane" id="tab4">
		<h4>Selected data set: <i>${selectedDataSet.name}</i></h4>
		<hr></hr>
		
		<#-- located in a seperate macro for readability-->
		<@annotationCheckboxes />	
	</div>
</#macro>

<#macro modalPopupDataSetUpload>
<div class="modal fade" id="dataset-upload-modal">
  	<div class="modal-header">
  		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
  		<h4>Create a new data set</h4>
  	</div>
  	
  	<#--bootstrap - jquery file upload form-->
  	<form role="form" action="${context_url}/create-new-dataset-from-tsv" method="post" enctype="multipart/form-data" boundary="_FPSEP91465b27654aa128979fb2_">
  		<div class="modal-body">	
			<div class="form-group has-succes">	
				<label>Name of your new dataset</label>
				<input type="text" id="dataset-name" name="dataset-name" required
					class="form-control" placeholder="enter your data set name here">
			</div>
			
			<div class="form-group">
				<div class="fileupload fileupload-new" data-provides="fileupload">
					<div class="input-group">
						
						<div class="form-control uneditable-input"><i class="icon-file fileupload-exists"></i> 
							<span class="fileupload-preview"></span>
						</div>
			
						<div class="input-group-btn">
			                <a class="btn btn-default btn-file">
			                    <span class="fileupload-new">Select file</span>
			                    <span class="fileupload-exists">Change</span>
			                    <input name="file-input-field" type="file" class="file-input"/>
			            	</a>
					            	
			                <a href="#" class="btn btn-default fileupload-exists" data-dismiss="fileupload">
			                	Remove
			                </a>           
			            </div>
			            
			        </div>
			    </div>
			</div>
		</div>	
		
		<div class="modal-footer">
	    	<a><button id="createDataSet" type="submit" class="btn btn-primary">Add</button></a>
	    	<a href="#dataset-upload-modal" data-toggle="modal" class="btn">Cancel</a>
		</div>
	</form>
</div>
</#macro>

<#macro annotationCheckboxes>
	<form role="form-horizontal">
		<div class="form-group">
			<div class="control">
				<h5>Annotations available</h5>
				<hr></hr>
				<#list allAnnotators?keys as annotator>
					<#if allAnnotators[annotator]>
						<label class="checkbox">
							<input type="checkbox" class="checkbox" name="${annotator}" id="${annotator}_id" value="${annotator}"> ${annotator}
						</label>							
					</#if>
				</#list>
				
				<hr></hr>
				<h5>Annotations not available
				<a id="disabled-tooltip" 
				title= "These annotations are not available for the selected data set because the data set does not contain the correct data" 
					data-toggle="tooltip" data-placement="top-right"><span 
							class="icon icon-question-sign"></span></a>
							
							</h5> 
				<hr></hr>
				
				<#list allAnnotators?keys as annotator>
					<#if allAnnotators[annotator]>
						<#-- Do nothing-->
					<#else>
						<label class="checkbox">
							<input type="checkbox" class="checkbox" name="${annotator}" id="${annotator}_id" value="${annotator}" disabled> ${annotator}
							
						</label>	
					</#if>
				</#list>
			</div>	
		</div>
	</form>	
</#macro>

<script>
	$("#dataset-select").change(function() {
		//alert("Handler has changed!");
		window.location.href="/menu/main/annotateUI/change-current-dataset";
	});

	
	$("#disabled-tooltip").tooltip();
	
	$("#rootwizard").bootstrapWizard({'tabClass': 'nav nav-tabs'});
	$("#dataset-select").chosen();
		
	// disable the filtering tabs for now
	$("#rootwizard").bootstrapWizard('disable', 1);
	$(".tab2").click(function(){return false;});
	
	$("#rootwizard").bootstrapWizard('disable', 2);
	$(".tab3").click(function(){return false;});
</script>

<@footer />
