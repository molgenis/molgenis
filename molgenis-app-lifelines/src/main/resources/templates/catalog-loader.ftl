<!DOCTYPE html>
<html>
	<head>
		<title>Catalog loader plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/protocolviewer.css" type"text/css">
		<link rel="stylesheet" href="/css/loaders.css" type="text/css">
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
		<script type="text/javascript">
			$(function() {
				parent.hideSpinner();
				
				$('#catalogForm').submit(function() {
					$('#submitButton').attr("disabled", "disabled");
					parent.showSpinner(); 
					return true;
				});
			});
		</script>
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
							<p id="loader-title" class="box-title">Choose a catalog to load</p>
							<#if catalogs?size == 0>
								<p>No catalogs found</p>
							<#else>
								<form id="catalogForm" name="catalogForm" method="post" action="/plugin/catalog/load">
									<div id="resultsTable">
										<table class="table table-striped table-hover listtable selection-table">
											<thead>
												<th></th>
												<th>Id</th>
												<th>Name</th>
											</thead>
											<#assign foundCatalog = false>
											<tbody>
												<#list catalogs as catalog>
													<tr>
														<td class="listEntryRadio">
															<#if catalog.loaded>
																LOADED
															<#else>
																<input id="catalog_${catalog.id?c}" type="radio" name="id" value="${catalog.id?c}" <#if !foundCatalog>checked<#assign foundCatalog = true></#if> >
															</#if>
														</td>
														<td class="listEntryId">
															<label for="catalog_${catalog.id?c}">${catalog.id?c}</label>
														</td>
														<td>
															<label for="catalog_${catalog.id?c}">${catalog.name}</label>
														</td>
													</tr>
												</#list>
											</tbody>
										</table>
									</div>
									<#if foundCatalog>
										<input id="submitButton" type="submit" class="btn pull-right" value="Load" />
									</#if>
								</form>
							</#if>
						</div>
					</#if>
				</div>
				<div class="span3"></div>
			</div>
		</div>
	</body>
</html>