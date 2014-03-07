<#macro fileupload_panel>
	<#--Panel 1: Input variants, manual or file(s)-->
	<div class="tab-pane" id="tab1">
			
		<@modal_popup_dataset_upload />
		<a type="button" class="btn" href="#dataset-upload-modal" data-toggle="modal">Upload a new Data set</a>
		
		<hr></hr>
		
		<div class="row-fluid">
			<div class="controls">
			<label class="control-label" for="dataset-select">Choose a dataset:</label>
				<div class="dataset-select-position-container">
					<select data-placeholder="Choose a Dataset" id="dataset-select">
						<#list dataSets as dataSet>
							<option value="/api/v1/dataset/${dataSet.id?c}" name="${dataSet.name}"<#if dataSet.identifier == selectedDataSet.identifier> selected</#if>>${dataSet.name}</option>
						</#list>
					</select>
				</div>
			</div>
		</div>	
		
	</div>
</#macro>

<#macro modal_popup_dataset_upload>
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