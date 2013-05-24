<!DOCTYPE html>
<html>
	<head>
		<title>Catalog loader plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/molgenis-main.css" type="text/css">
		<link rel="stylesheet" href="/css/protocolviewer.css" type"text/css">
		<link rel="stylesheet" href="/css/catalog-loader.css" type="text/css">
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
					<#if catalogs??>	
						<div class="well">
							<p id="catalog-loader-title" class="box-title">Choose a catalog to load</p>
						
							<form name="catalogForm" method="post" action="/plugin/catalog/load" onsubmit="$('#spinner').modal('show'); return true;">
								<div id="catalogsTable">
									<table class="table table-striped table-hover listtable selection-table">
										<thead>
											<th></th>
											<th>Id</th>
											<th>Name</th>
										</thead>
										<#assign foundCatalog = false>
										<#if catalogs?size == 0>
											<tr><td>No catalogs found</td></tr>
										</#if>
										<tbody>
										<#list catalogs as catalog>
											<tr>
												<td class="catalogRadio">
													<#if catalog.loaded>
														LOADED
													<#else>
														<input id="catalog_${catalog.id}" type="radio" name="id" value="${catalog.id}" <#if !foundCatalog>checked<#assign foundCatalog = true></#if> >
													</#if>
												</td>
												<td class="catalogId">
													<label for="catalog_${catalog.id}">${catalog.id}</label>
												</td>
												<td>
													<label for="catalog_${catalog.id}">${catalog.name}</label>
												</td>
											</tr>
										</#list>
										</tbody>
									</table>
								</div>
								<#if foundCatalog>
									<input type="submit" class="btn pull-right" value="Load catalog" />
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