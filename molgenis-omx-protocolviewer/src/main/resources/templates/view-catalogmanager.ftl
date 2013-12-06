<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["ui.dynatree.css", "catalogmanager.css"]>
<#assign js=["jquery-ui-1.9.2.custom.min.js", "jquery.dynatree.min.js", "catalogmanager.js"]>
<@header css js/>
	<div class="row-fluid">
		<div class="span8 offset2">
		<#if catalogs??>	
			<div class="well">
				<div class="row-fluid">
					<p id="loader-title" class="box-title">Choose a catalog to load</p>
				<#if catalogs?size == 0>
					<p>No catalogs found</p>
				<#else>
					<form id="catalogForm" name="catalogForm" method="post" action="${context_url}/load">
						<div class="row-fluid">
							<div class="span6">	
								<div id="resultsTable">
									<table class="table table-striped table-hover listtable selection-table">
										<thead>
											<tr>
												<th></th>
												<th>Id</th>
												<th>Name</th>
											</tr>
										</thead>
									<#assign foundCatalog = false>
										<tbody>
									<#list catalogs as catalog>
										<tr>
											<td class="listEntryRadio">
												<input id="catalog_${catalog.id}" type="radio" name="id" value="${catalog.id}" data-loaded="<#if catalog.loaded>true<#else>false</#if>" <#if !foundCatalog>checked<#assign foundCatalog = true></#if> >
											</td>
											<td class="listEntryId">
												<label for="catalog_${catalog.id}">${catalog.id}</label>
											</td>
											<td>
												<label for="catalog_${catalog.id}">${catalog.name}<#if catalog.loaded><p class="text-success pull-right">Loaded</p></#if></label>
											</td>
										</tr>
									</#list>
										</tbody>
									</table>
								</div>
							</div>
							<div class="span6">
								<div id="catalog-preview">
									<div id="catalog-preview-info">
									</div>
									<div id="catalog-preview-tree">
									</div>
								</div>
							</div>
						</div>
					<#if foundCatalog>
						<input id="loadButton" type="submit" name="load" class="btn pull-right" value="Load" />
					</#if>
					</form>
				</#if>
				</div>
			</div>
		</#if>
		</div>
	</div>
	<script>
	$(function() {
		parent.hideSpinner();
		
		$('#catalogForm').submit(function() {
			parent.showSpinner(); 
			return true;
		});
	});
	</script>
<@footer/>