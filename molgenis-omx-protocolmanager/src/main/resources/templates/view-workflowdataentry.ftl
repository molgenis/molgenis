<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["chosen.css", "jquery.bootstrap.wizard.css", "workflowdataentry.css"]>
<#assign js=["chosen.jquery.min.js", "jquery.bootstrap.wizard.min.js", "workflowdataentry.js"]>
<@header css js/>
			<div class="row-fluid">
				<div class="control-group pull-right form-horizontal">
					<label class="control-label" for="workflow-select">Choose a protocol:</label>
					<div class="controls">
		    			<select id="workflow-application-select">
							<#list workflows as workflow>
								<option value="${workflow.id}">${workflow.name}</option>
							</#list>
		      			</select>
					</div>
				</div>
			</div>
			<div class="row-fluid">
				<div id="workflow-application-container">
					<div id="workflow-wizard">
						<ul id="workflow-nav">
						</ul>
						<div id="workflow-nav-content" class="tab-content">
						</div>
						<ul class="pager wizard">
							<li class="previous first" style="display:none;"><a href="#">First</a></li>
							<li class="previous"><a href="#">Previous</a></li>
							<li class="next last" style="display:none;"><a href="#">Last</a></li>
						  	<li class="next"><a href="#">Next</a></li>
						</ul>
					</div>
				</div>
			</div>
<@footer/>