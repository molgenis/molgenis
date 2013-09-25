<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["chosen.css", "entityexplorer.css"]>
<#assign js=["chosen.jquery.min.js", "entityexplorer.js"]>
<@header css js/>
	<div class="row-fluid">
		<div class="span2"></div>
		<div class="span8">
			<div class="well">
				<div class="row-fluid">
					<div class="span6">
						<label class="control-label" for="entity-select">Entity class:</label>
						<div class="controls">
							<select data-placeholder="Please Select" id="entity-select">
						<#list entities as entity>
								<option value="${entity?lower_case}"<#if entity == selectedEntity> selected</#if>>${entity}</option>
						</#list>
				      		</select>
						</div>
			      	</div>
			      	<div class="span6">
				      	<label class="control-label" for="entity-instance-select">Entity instance:</label>
						<div class="controls">
							<select data-placeholder="Please Select" id="entity-instance-select">
						<#if entityInstances??>
							<#list entityInstances as entityInstance>
									<option value="/api/v1/${selectedEntity?lower_case}/${entityInstance.id?c}"<#if entityInstance.id == selectedEntityInstance.id> selected</#if>>${entityInstance.name}</option>
							</#list>
						</#if>
					      	</select>
					    </div>
			      	</div>
				</div>
				<div class="row-fluid">
					<div class="span6">
						<table class="table table-condensed" id="entity-table">
						</table>
					</div>
				</div>
			</div>
		</div>
		<div class="span2"></div>
	</div>	
	<div class="row-fluid">
		<div id="entity-search-results-header" class="pull-left">
		</div>
	</div>
	<div class="row-fluid">
		<div id="entity-search-results">
		</div>
	</div>
<@footer/>