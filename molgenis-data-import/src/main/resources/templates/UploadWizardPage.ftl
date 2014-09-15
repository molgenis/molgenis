<form method="post" id="wizardForm" name="wizardForm" enctype="multipart/form-data" action="" role="form">
    <div style="padding-top: 25px">
	    
	    <div>
	    	<h4>Upload a file</h4>
	    	<input type="file" name="upload" data-filename-placement="inside" title="Select a file...">
	    	<hr></hr>
	    </div>
	    
	    <div class="row">
	        <div class="col-md-4">
				<div class="panel">
					<div class="panel-heading">
		    			<h3 class="panel-title">
		    				<a data-toggle="collapse" data-target="#upload-options-collapse" href="#upload-options-collapse">Advanced options</a>
		    			</h3>
		  			</div>
		  			<div id="upload-options-collapse" class="panel-collapse collapse">
		  				<div class="panel-body">
							<div class="col-md-12">
								<label class="radio">
									<input type="radio" name="entity_option" value="add_update">Add entities / update existing
								</label>
								<span>Importer adds new entities or updates existing entities<span>
								<label class="radio">
									<input type="radio" name="entity_option" value="add" checked>Add entities		
								</label>
								<span>Importer adds new entities or fails if entity exists</span>
                                <label class="radio">
                            		<input type="radio" name="entity_option" value="update">Update Entities
                            	</label>
                            	<span>Importer updates existing entities or fails if entity does not exist</span>
                        	</div>
			  			</div>
		  			</div>
				</div>
			</div>
	    </div>
	    
	</div> 
</form>