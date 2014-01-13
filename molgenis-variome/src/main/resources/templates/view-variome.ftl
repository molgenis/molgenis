<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["variome.css", "jquery-ui-1.10.3.custom.css", "chosen.css", "bootstrap-fileupload.css"]>
<#assign js=["jquery-ui-1.10.3.custom.min.js", "chosen.jquery.min.js", "bootstrap-fileupload.js"]>

<@header css js />

<div class="span12">
	<div id="accordion">
	
		<#--Panel 1: Input variants, manual or file(s)-->
		<h3>Select variants</h3>
		<div>
			<div id="variant-selection-tabs">
			
			 	<ul>
				    <li><a href="#panel-1-tab-1"><span>Upload file</span></a></li>
				    <li><a href="#panel-1-tab-2"><span>Manual input</span></a></li>
				    <li><a href="#panel-1-tab-3"><span>ZIP upload</span></a></li>
				    <li><a href="#panel-1-tab-4"><span>Database select</span></a></li>
			  	</ul>
			  	
			  	<div id="panel-1-tab-1">
			    	<form role="form" action="${context_url}/upload-vcf" method="post" enctype="multipart/form-data">
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
						                    <input type="file" class="file-input"/>
						            	</a>
						            	
						                <a href="#" class="btn btn-default fileupload-exists" data-dismiss="fileupload">
						                	Remove
						                </a>
						                
						            </div>
						        </div>
						    </div>
						</div>
				
						<button type="submit" class="btn">Add</button>	
					</form>
			  	</div>
			  	
			  	<div id="panel-1-tab-2">
			  		<h5>Copy paste your variants in the text box below</h5>
			  		<form role="form" action="${context_url}/upload-pasted-vcf" method="post">
			  			<label>
			    			<textarea class="input-block-level" rows="3"></textarea>
			    		</label>
			    	
			    		<button type="submit" class="btn">Add</button>
			    	</form>	
			  	</div>
			  	
			  	<div id="panel-1-tab-3">
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
						                    <input type="file" class="file-input"/>
						                </a>
						                
						                <a href="#" class="btn btn-default fileupload-exists" data-dismiss="fileupload">
						                	Remove
						                </a>
						                
						            </div>
						        </div>
						    </div>
						</div>
				
						<button type="submit" class="btn">Add</button>
				</div>
				
				<div id="panel-1-tab-4">
			    	<div class="checkbox">
						<label>
  							<input type="checkbox"> GoNL Variant Database
						</label>
					</div>
					
					<div class="checkbox">
						<label>
  							<input type="checkbox"> 1000 Genome Variant Database
						</label>
					</div>
				</div>
			</div>	
			
			<hr></hr>
			
			<h5>Selected Variant Sources</h5>
			<span><h7>No variants selected</h7></span>
			
		</div>
		
		<#--Panel 2: Input gene panels or genomic locations-->
		<h3>Filter by: Genomic locations / Gene panels / Clinical Databases</h3>
		<div>
			<div id="region-selection-tabs">
				<ul>
				    <li><a href="#panel-2-tab-1"><span>Gene panel input</span></a></li>
				    <li><a href="#panel-2-tab-2"><span>Region select</span></a></li>
				    <li><a href="#panel-2-tab-3"><span>upload BED</span></a></li>
				    <li><a href="#panel-2-tab-4"><span>Predefined gene panels</span></a></li>
			  	</ul>
			  	
			  	<div id="panel-2-tab-1">
			  		<h5>Paste your gene list in the text area below</h5>
			  		<select name="" class="form-control">
					    <option value="gene-id-HUGO">HUGO Symbols (e.g. BRCA1)</option>
					    <option value="gene-id-ensemble">Ensemble IDs (e.g. ENSG00000012048)</option>
					</select>
					
			  		<form role="form" action="${context_url}/upload-pasted-gene-list" method="post">
			  			<label>
			    			<textarea class="input-block-level" rows="3"></textarea>
			    		</label>
			    	
			    		<button type="submit" class="btn">Add</button>
				    </form>	
			  	</div>
			  	
			  	<div id="panel-2-tab-2">
			  		<form class="form-horizontal" role="form" action="${context_url}/upload-region-filter" method="post">
			  			<div class="form-group">
			  				<label>Chromosome</label>
			  				<input class="form-control" placeholder="chromosome">
			  			</div>
			  			
			  			<div class="form-group">
			  				<label>Start position (bp)</label>
			  				<input class="form-control" placeholder="start position">
			  			</div>
			  			
			  			<div class="form-group">
			  				<label>Stop position (bp)</label>
			  				<input class="form-control" placeholder="stop position">
			  			</div>
			  			<br />
			  			<button type="submit" class="btn">Add</button>
			  		</form>
			  	</div>
			  	
			  	<div id="panel-2-tab-3">
			  		<form role="form" action="${context_url}/upload-bed" method="post" enctype="multipart/form-data">
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
						                    <input type="file" class="file-input"/>
						            	</a>
						            	
						                <a href="#" class="btn btn-default fileupload-exists" data-dismiss="fileupload">
						                	Remove
						                </a>
						                
						            </div>
						        </div>
						    </div>
						</div>
				
						<button type="submit" class="btn">Add</button>	
					</form>
			  	</div>
			  	
			  	<div id="panel-2-tab-4">
			  		<div class="checkbox">
						<label>
  							<input type="checkbox"> Onco Panel
						</label>
  					</div>
  					
  					<div class="checkbox">
						<label>
  							<input type="checkbox"> Cardiac Panel
						</label>
  					</div>
  					
  					<div class="checkbox">
						<label>
  							<input type="checkbox"> Preconception Panel
						</label>
					</div>
			  	</div>
			  	
			</div>
			
			<hr></hr>
			
			<h5>Selected Regions</h5>
			<h7>No regions selected</h7>
			
		</div>
		
		<#--Panel 3: Phenotype selection-->
		<h3>Filter by: Disease / Phenotype / Symptoms</h3>
		<div>
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
		
		<#--Panel 4: Annotation tool / database selection-->
		<h3>Select variant annotation columns</h3>
		<div>
			<div class="checkbox">
				<label>
					<input type="checkbox"> Allele specific expression database
				</label>
			</div>
			
			<div class="checkbox">
				<label>
					<input type="checkbox"> dbSFNP annotation tools
				</label>
			</div> 
			
			<div class="checkbox">
				<label>
					<input type="checkbox"> SnpEFF annotation tools
				</label>
			</div>
			
			<div class="checkbox">
				<label>
					<input type="checkbox"> CADD database
				</label>
			</div>
		</div>
	</div>
	
	<hr></hr>
	
	<div>
		<form role="form" action="${context_url}/execute-variant-app" method="post">
			<button type="submit" class="btn">Go</button>
		</form>
	</div>
</div>	

<script>
	$("#accordion").accordion({ heightStyle: false});
	$("#variant-selection-tabs").tabs();
	$("#region-selection-tabs").tabs();
	$(".phenotypeSelect	").chosen();
</script>

<@footer />