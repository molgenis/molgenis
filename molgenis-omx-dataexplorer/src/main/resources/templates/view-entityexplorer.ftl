<#if enable_spring_ui>
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["chosen.css", "entityexplorer.css"]>
<#assign js=["chosen.jquery.min.js", "entityexplorer.js"]>
<@header css js/>
	<div class="row-fluid">
		<div class="well">
				<div class="row-fluid">
					<div class="span3">
						<label class="control-label" for="entity-select">Entity class:</label>
						<div class="controls">
							<select data-placeholder="Please Select" id="entity-select">
						<#list entities as entity>
								<option value="${entity?lower_case}"<#if entity == selectedEntity> selected</#if>>${entity}</option>
						</#list>
				      		</select>
						</div>
			      	</div>
			      	<div class="span3">
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
			<div class="row-fluid">
				<div id="entity-search-results-header" class="pull-left">
				</div>
			</div>
			<div class="row-fluid">
				<div id="entity-search-results">
				</div>
			</div>	
	</div>
<@footer/>
<#else>
<!DOCTYPE html>
<html>
	<head>
		<title>Data explorer plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/chosen.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/entityexplorer.css" type="text/css">
	<#if app_href_css??>
        <link rel="stylesheet" href="${app_href_css}" type="text/css">
    </#if>
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/chosen.jquery.min.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/entityexplorer.js"></script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="well">
				<div class="row-fluid">
					<div class="span3">
						<label class="control-label" for="entity-select">Entity class:</label>
						<div class="controls">
							<select data-placeholder="Please Select" id="entity-select">
						<#list entities as entity>
								<option value="${entity?lower_case}"<#if entity == selectedEntity> selected</#if>>${entity}</option>
						</#list>
				      		</select>
						</div>
			      	</div>
			      	<div class="span3">
				      	<label class="control-label" for="entity-instance-select">Entity instance:</label>
						<div class="controls">
							<select data-placeholder="Please Select" id="entity-instance-select">
						<#list entityInstances as entityInstance>
								<option value="/api/v1/${selectedEntity?lower_case}/${entityInstance.id?c}"<#if entityInstance.id == selectedEntityInstance.id> selected</#if>>${entityInstance.name}</option>
						</#list>
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
			<div class="row-fluid">
				<div id="entity-search-results-header" class="pull-left">
				</div>
			</div>
			<div class="row-fluid">
				<div id="entity-search-results">
				</div>
			</div>
		</div>
	</body>
</html>
</#if>