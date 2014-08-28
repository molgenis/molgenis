<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["entityexplorer.css"]>
<#assign js=["entityexplorer.js"]>
<@header css js/>
	<#if dataExplorerUrl??>
		<script>top.molgenis.setDataExplorerUrl('${dataExplorerUrl}');</script>
	</#if>
	<div class="row">
		<div class="col-md-2"></div>
		<div class="col-md-8">
			<div class="well">
				<div class="row">
					<div class="col-md-6">
						<label class="col-md-3 control-label" for="entity-select">Entity class:</label>
						<div class="col-md-9">
							<select class="form-control" data-placeholder="Please Select" id="entity-select">
						<#list entities as entity>
								<option value="${entity?lower_case}"<#if entity == selectedEntity> selected</#if>>${entity}</option>
						</#list>
				      		</select>
						</div>
			      	</div>
			      	<div class="col-md-6">
				      	<label class="col-md-3 control-label" for="entity-instance-select">Entity instance:</label>
						<div class="col-md-9">
							<select class="form-control" data-placeholder="Please Select" id="entity-instance-select">
						<#if entityInstances??>
							<#list entityInstances as entityInstance>
									<option value="/api/v1/${selectedEntity?lower_case}/${entityInstance.id?c}"<#if entityInstance.id == selectedEntityInstance.id> selected</#if>>${entityInstance.name}</option>
							</#list>
						</#if>
					      	</select>
					    </div>
			      	</div>
				</div>
				<div class="row">
					<div class="col-md-6">
						<table class="table table-condensed" id="entity-table">
						</table>
					</div>
				</div>
			</div>
		</div>
		<div class="col-md-2"></div>
	</div>	
	<div class="row">
		<div id="entity-search-results-header" class="pull-left">
		</div>
	</div>
	<div class="row">
		<div id="entity-search-results">
		</div>
	</div>
<@footer/>