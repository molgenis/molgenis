<#macro annotation_select_tab>
	<#--Panel 4: Annotation tool / database selection-->
	<div class="tab-pane" id="tab4">
		<#-- located in a seperate macro for readability-->
		<form id="execute-annotation-app" role="form" action="${context_url}/execute-annotation-app" method="post">
			<h5>Selected your data set: ${selectedDataSet.name}</h5> 

			<div class="row-fluid">
				<div class="controls">
					<select data-placeholder="Choose a Dataset" id="dataset-select">
						<#list dataSets as dataSet>
							<option value="/api/v1/dataset/${dataSet.id?c}" <#if dataSet.identifier == selectedDataSet.identifier> selected</#if>>${dataSet.name}</option>
						</#list>
					</select>
				</div>
			</div>	
			
			<hr></hr>
			
			<div class="form-group">
				<div class="control">
					<h5>Annotations available</h5>
					<hr></hr>
					<#list allAnnotators?keys as annotator>
						<#if allAnnotators[annotator]>
							<label class="checkbox">
								<input type="checkbox" class="checkbox" id="annotatorNames" name="annotatorNames" value="${annotator}"> ${annotator}
							</label>							
						</#if>
					</#list>
					
					<input type="hidden" name="dataSetIdentifier" value="${selectedDataSet.identifier}"></input>
					
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
								<input type="checkbox" class="checkbox" id="annotatorNames" name="annotatorNames" value="${annotator}" disabled> ${annotator}
								
							</label>	
						</#if>
					</#list>
				</div>	
			</div>	
		</form>	
	</div>
</#macro>

		
	<div id="test"></div>
