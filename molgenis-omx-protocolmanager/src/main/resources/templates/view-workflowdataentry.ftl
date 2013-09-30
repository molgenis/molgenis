<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["chosen.css"]>
<#assign js=["chosen.jquery.min.js", "workflowdataentry.js"]>
<@header css js/>
			<div class="row-fluid">
				<div class="span3">
					<div class="control-group">
						<label class="control-label" for="workflow-select">Choose a workflow:</label>
						<div class="controls">
			    			<select id="workflow-select" name="workflowId">
								<#list workflows as workflow>
									<option value="${workflow.id}">${workflow.name}</option>
								</#list>
			      			</select>
						</div>
					</div>
				</div>
				<div id="workflow-data-entry-container" class="span9">
					<ul id="workflow-nav" class="nav nav-tabs">
					</ul>
					<div id="workflow-nav-content" class="tab-content">
					</div>
				</div>
			</div>
<@footer/>