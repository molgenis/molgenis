<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["chosen.css", "protocolmanager.css"]>
<#assign js=["chosen.jquery.min.js", "protocolmanager.js", "${resultsTableJavascriptFile}"]>
<@header css js/>
		<script type="text/javascript">
			$(function() {
				window.top.molgenis.fillWorkflowSelect(function() {
					<#-- select first dataset -->
					$('#workflow-select option:first').val();
					<#-- fire event handler -->
					$('#workflow-select').change();
					<#-- use chosen plugin for dataset select -->
					$('#workflow-select').chosen();
				});
			});
		</script>
			<div class="row-fluid">
				<div id="modals"></div>
				<div class="span9">
					<div id="workflow-select-container" class="control-group form-horizontal">
						<div class="controls pull-right">
							<label class="control-label" for="workflow-select">Choose a workflow:</label>
							<form id="workflowForm" method="post" action="${context_url}/select">
								<select data-placeholder="Choose a workflow" id="workflow-select" name="workflowId" form="workflowForm">
									<#list workflows as workflow>
										<option value="${workflow.id}">${workflow.name}</option>
									</#list>
						      	</select>
						      	<input type="submit" class="btn" value="Select"/>	
					      	</form>
						</div>					
					</div>	
				</div>
			</div>
<@footer/>