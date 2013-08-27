<#if enable_spring_ui>
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
						<select data-placeholder="Choose a workflow" id="workflow-select">
						</select>
					</div>
				</div>
				<a id="next-button" class="btn" href="#">Next</a>	
			</div>
		</div>
<@footer/>
<#else>
<!DOCTYPE html>
<html>
	<head>
	
		<title>Protocol manager plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/chosen.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap-datetimepicker.min.css" type="text/css">
		<link rel="stylesheet" href="/css/protocolmanager.css" type="text/css">
		<link rel="stylesheet" href="/css/ui.dynatree.css" type="text/css">
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="/js/chosen.jquery.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/protocolmanager.js"></script>
		<script type="text/javascript" src="${resultsTableJavascriptFile}"></script>
		<script type="text/javascript" src="/js/jquery.dynatree.min.js"></script>
		<script type="text/javascript" src="/js/bootstrap-datetimepicker.min.js"></script>
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
		
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				<div id="modals"></div>
				<div class="span9">
					<div id="workflow-select-container" class="control-group form-horizontal">
						<div class="controls pull-right">
							<label class="control-label" for="workflow-select">Choose a workflow:</label>
							<select data-placeholder="Choose a workflow" id="workflow-select">
							</select>
						</div>
					</div>
					<a id="next-button" class="btn" href="#">Next</a>	
				</div>
			</div>
		</div>		
		
	</body>
</html>
</#if>