<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['metadataexplorer.css']>
<#assign js=['metadataexplorer.js', 'jquery.bootstrap.pager.js']>
<@header css js/>
	
<h1>Entities</h1>	
<div class="subtitle">This is a listing of all datasets available in ${molgenis_ui.title?html}</div>

<div class="row-fluid">
	<form id="model-search-form" action="" method="POST">
		<div class="input-append span4">
			<span class="search-label">Search:</span>
			<input class="span8" id="search-input" name="searchTerm" type="text" placeholder="Type here your search" autofocus="autofocus" value="${metaDataSearchForm.searchTerm!}">
			<button class="btn" type="button" id="clear-button"><i class="icon-large icon-remove"></i></button>
			<button class="btn" type="submit" id="search-button"><i class="icon-large icon-search"></i></button>
		</div>	
		<div class="span8">
			<span class="search-label">Entity types:</span>
			<input type="hidden" value="on" name="_entityClassTypes"/><#-- This is a marker for spring that all checkboxes are deselected -->
			<#list entityClassTypes as type>
				<label><input type="checkbox" name="entityClassTypes" value="${type}" <#if metaDataSearchForm.entityClassTypes?? && metaDataSearchForm.entityClassTypes?seq_contains(type)>CHECKED</#if> /> ${type}</label>
			</#list>
		</div>
		<input type="hidden" name="page" value="${metaDataSearchForm.page}" />
	</form>
</div>			
					
<div id="pager"></div>
					
<#list entityClassModels as model>
	<div class="well">
		<div class="row-fluid entity-class-header">
			<h3>${model.entityClass.fullName?html}</h3> 
			<i>(${model.entityClass.entityClassIdentifier})</i>
			<#if model.explorerUri??><a href="${model.explorerUri}" class="btn entity-btn">Explore data</a></#if>
			<#if model.formUri??><a href="${model.formUri}" class="btn entity-btn">Edit</a></#if>
		</div>
		<div class="row-fluid">
			<div class="span2">Type:</div>
			<div class="span10">${model.entityClass.type}</div>
		</div>
		<#if model.entityClass.description?? && model.entityClass.description != ''>
			<div class="row-fluid">
				<div class="span2">Description:</div>
				<div id="entityClass-${model.entityClass.id?c}" class="span10">${limit(model.entityClass.description?html, 150, 'entityClass-${model.entityClass.id?c}')}</div>
			</div>
		</#if>
		<#if model.entityClass.tags?size &gt; 0>
			<div class="row-fluid">
				<div class="span2">Tags:</div>
				<div class="span10">
					<#list model.entityClass.tags as tag>
						${tag.name}<#if tag != model.entityClass.tags?last>,</#if>
					</#list>
				</div>
			</div>
		</#if>
		<#if model.entityClass.homepage?? && model.entityClass.homepage != ''>
			<div class="row-fluid">
				<div class="span2">Homepage:</div>
				<div class="span10"><a href="${model.entityClass.homepage}" target="_blank">${model.entityClass.homepage}</a></div>
			</div>
		</#if>
	</div>
</#list>

<i>${nrItems} items found</i>

<script>
	var nrItems = ${nrItems};
	var nrItemsPerPage = ${nrItemsPerPage};
</script>
<@footer/>