<#macro annotationselect_panel>
	<#--Panel 4: Annotation tool / database selection-->
	<div class="tab-pane" id="tab4">
		<form id="execute-annotation-app" role="form">
			  
			<h4>Your selected dataset: <div class=lead id="selected-dataset-name"></div></h4> 
			
			<div class="form-group">
				<div class="control">
					<div class="row-fluid">
						<div class="span1"></div>
						<div class="span5">
							<h5>Annotations available</h5>
							
							<hr></hr>
							
							<div id="annotator-checkboxes-enabled"></div>
					
						</div>
						<div class="span5">
							<h5>Annotations not available
								<a id="disabled-tooltip" data-toggle="tooltip"
									title= "These annotations are not available for the selected data set because: 1) the data is not available or 2) your data set does not contain the correct columns"> 
										
									<span class="icon icon-question-sign"></span>
								</a>
							</h5> 
							
							<hr></hr>
							
							<div id="annotator-checkboxes-disabled"></div>
						</div>
					</div>		
				</div>	
			</div>
			
			<hr></hr>
			
			<label>
      			<input type="checkbox" name="createCopy"> Create a copy
    		</label>
			
			<br>
			
			<input type="hidden" value="" id="dataset-identifier" name="dataset-identifier">
            <button id="execute-button" class="btn">Run annotation</button>
            
		</form>
	</div>
</#macro>