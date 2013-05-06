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
						<select id="entity-select">
					      	<option value="" disabled selected>Please Select</option>
						<#list entities as entity>
							<option value="${entity?lower_case}">${entity}</option>
						</#list>
				      	</select>
			      	</div>
			      	<div class="span3">
						<select id="entity-instance-select">
				      	</select>
			      	</div>
				</div>
			</div>
			<div class="row-fluid">
				<table class="table table-striped table-condensed" id="entity-table">
				</table>
			</div>
			<div class="row-fluid">
				<ul id="entity-search-results">
				</ul>
			</div>
		</div>
	</body>
</html>