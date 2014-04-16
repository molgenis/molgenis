<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#import "metadata-macros.ftl" as m>

<#assign css=['metadataexplorer.css']>
<#assign js=['metadataexplorer.js', 'jquery.bootstrap.pager.js']>
<@header css js/>
	
<h1>Entities</h1>	
<div class="subtitle">This is a listing of all datasets available in ${molgenis_ui.title?html}</div>

<div class="row-fluid">
	<form id="model-search-form" action="">
		<div class="input-append span4">
			<span class="search-label">Search:</span>
			<input class="span8" id="search-input" name="searchTerm" type="text" placeholder="Type here your search" autofocus="autofocus" value="${metaDataSearchForm.searchTerm!}">
			<button class="btn" type="button" id="clear-button"><i class="icon-large icon-remove"></i></button>
			<button class="btn" type="submit" id="search-button"><i class="icon-large icon-search"></i></button>
		</div>	
		<div class="span8">
			<span class="search-label">Entity types:</span>
			<input type="hidden" value="on" name="_entityClassTypes"/><#-- This is a marker for spring if all checkboxes are deselected -->
			<#list entityClassTypes as type>
				<label><input type="checkbox" name="entityClassTypes" value="${type}" <#if metaDataSearchForm.entityClassTypes?? && metaDataSearchForm.entityClassTypes?seq_contains(type)>CHECKED</#if> /> ${type}</label>
			</#list>
		</div>
		<input type="hidden" name="page" value="${metaDataSearchForm.page}" />
	</form>
</div>			
					
<div id="pager"></div>
					
<#list entityClasses as entityClass>
	<@m.renderEntityClassInfo entityClass />
</#list>

<i>${nrItems} items found</i>

<script>
	var nrItems = ${nrItems};
	var nrItemsPerPage = ${nrItemsPerPage};
</script>
<@footer/>