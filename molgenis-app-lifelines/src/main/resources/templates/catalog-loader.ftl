<!DOCTYPE html>
<html>
	<head>
		<title>Catalog loader plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
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
						<p id="catalog-loader-title" class="box-title">Choose catalog to load</p>
						<form name="catalogForm" method="post" action="/plugin/catalog/load">
						
							<table class="table table-striped table-hover listtable selection-table">
								<#if catalogs?size == 0>
									<tr><td>No catalogs found</td></tr>
								</#if>
								<#list catalogs as catalog>
									<tr>
										<td class="catalogRadio">
											<input id="catalog_${catalog.id}" type="radio" name="id" value="${catalog.id}" <#if catalog_index == 0>checked</#if> >
										</td>
										<td>
											<label for="catalog_${catalog.id}">${catalog.name}</label>
										</td>
									</tr>
								</#list>
							</table>
							<#if catalogs?size != 0>
								<input type="submit" class="btn pull-right" value="Load catalog" />
							</#if>
						</form>
					</#if>
				</div>
				<div class="span3"></div>
			</div>
		</div>
	
	
		
	</body>
</html>