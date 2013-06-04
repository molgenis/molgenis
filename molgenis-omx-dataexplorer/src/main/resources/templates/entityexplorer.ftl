<!DOCTYPE html>
<html>
	<head>
		<title>Data explorer plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/chosen.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/entityexplorer.css" type="text/css">
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
								<option value="/api/v1/${selectedEntity?lower_case}/${entityInstance.id?c}"<#if entityInstance.id == selectedEntityInstance.id> selected</#if>>${entityInstance.identifier}</option>
						</#list>
					      	</select>
					    </div>
			      	</div>
				</div>
			</div>
			<div class="row-fluid">
				<table class="table table-striped table-condensed" id="entity-table">
				</table>
			</div>
			<div class="row-fluid">
				<div id="entity-search-results">
				</div>
			</div>
		</div>
	</body>
</html>