<#if enable_spring_ui>
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["chosen.css", "protocolmanager.css"]>
<#assign js=["chosen.jquery.min.js", "protocolmanager.js", "${resultsTableJavascriptFile}"]>
<@header css js/>
		<div class="row-fluid">
		<div id="modals"></div>
		<div class="span9">
			<div id="protocol-select-container" class="control-group form-horizontal">
				<div id="data-table-header" class="pull-left"></div>
				<div class="controls pull-right">
					<label class="control-label" for="protocol-select">Choose a protocol:</label>
					<select data-placeholder="Choose a Protocol" id="protocol-select">
					</select>
				</div>
			</div>
			<table id="protocol_data-table" class="table table-striped table-condensed">
			</table>
		</div>
		<a id="save-button" class="btn" href="#">Save</a>
		<a id="add-button" class="btn" href="#">Add row</a>
		<div class="feature-filter-dialog"></div>
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
		<script type="text/javascript" src="/js/${resultsTableJavascriptFile}"></script>
		<script type="text/javascript" src="/js/jquery.dynatree.min.js"></script>
		<script type="text/javascript" src="/js/bootstrap-datetimepicker.min.js"></script>
		<script type="text/javascript">
			$(function() {
				window.top.molgenis.fillProtocolSelect(function() {
					<#-- select first dataset -->
					$('#protocol-select option:first').val();
					<#-- fire event handler -->
					$('#protocol-select').change();
					<#-- use chosen plugin for dataset select -->
					$('#protocol-select').chosen();
				});
			});
			
		</script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
			<div id="modals"></div>
			<div class="span9">
				<div id="protocol-select-container" class="control-group form-horizontal">
					<div id="data-table-header" class="pull-left"></div>
					<div class="controls pull-right">
						<label class="control-label" for="protocol-select">Choose a protocol:</label>
						<select data-placeholder="Choose a Protocol" id="protocol-select">
						</select>
					</div>
				</div>
				<table id="protocol_data-table" class="table table-striped table-condensed">
				</table>
			</div>
		</div>
		<a id="save-button" class="btn" href="#">Save</a>
		<a id="add-button" class="btn" href="#">Add row</a>
		<div class="feature-filter-dialog"></div>
		
	</body>
</html>
</#if>