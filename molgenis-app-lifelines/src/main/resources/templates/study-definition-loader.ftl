<!DOCTYPE html>
<html>
	<head>
		<title>Studydefinition loader plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/molgenis-main.css" type="text/css">
		<link rel="stylesheet" href="/css/protocolviewer.css" type"text/css">
		<link rel="stylesheet" href="/css/loaders.css" type="text/css">
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				<div class="span3"></div>
				<div class="span6">
					<#if errorMessage??>
						<div class="alert alert-error">
							<button type="button" class="close" data-dismiss="alert">&times;</button>
							${errorMessage}
						</div>
					</#if>
			
					<#if successMessage??>
						<div class="alert alert-success">
							<button type="button" class="close" data-dismiss="alert">&times;</button>
							${successMessage}
						</div>
					</#if>		
					<#if studyDefinitions??>	
						<div class="well">
							<p id="loader-title" class="box-title">Choose a studydefinition to load</p>
						
							<form name="studyDefinitionForm" method="post" action="/plugin/studydefinition/load" onsubmit="$('#spinner').modal('show'); return true;">
								<div id="resultsTable">
									<table class="table table-striped table-hover listtable selection-table">
										<thead>
											<th></th>
											<th>Id</th>
											<th>Name</th>
										</thead>
										<#assign foundStudyDefinition = false>
										<#if studyDefinitions?size == 0>
											<tr><td>No studydefinitions found</td></tr>
										</#if>
										<tbody>
										<#list studyDefinitions as studyDefinition>
											<tr>
												<td class="listEntryRadio">
													<#if studyDefinition.loaded>
														LOADED
													<#else>
														<input id="catalog_${studyDefinition.id}" type="radio" name="id" value="${studyDefinition.id}" <#if !foundStudyDefinition>checked<#assign foundStudyDefinition = true></#if> >
													</#if>
												</td>
												<td class="listEntryId">
													<label for="catalog_${studyDefinition.id}">${studyDefinition.id}</label>
												</td>
												<td>
													<label for="catalog_${studyDefinition.id}">${studyDefinition.name}</label>
												</td>
											</tr>
										</#list>
										</tbody>
									</table>
								</div>
								<#if foundStudyDefinition>
									<input type="submit" class="btn pull-right" value="Load studydefinition" />
								</#if>
							</form>
						</div>
					</#if>
				</div>
				<div class="span3"></div>
			</div>
		</div>
	
		<#include "spinner.ftl">
		
	</body>
</html>