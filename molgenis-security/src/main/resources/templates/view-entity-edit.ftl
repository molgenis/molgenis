<#include "resource-macros.ftl">
<#import "molgenis-input-elements.ftl" as input>
<#import "form-macros.ftl" as f>

<form class="form-horizontal" id="entity-form" role="form" method="POST" action="/api/v1/${entityName}<#if form.primaryKey?is_number>/${form.primaryKey?c}<#else>/${form.primaryKey}</#if>?_method=PUT">
	<#list form.metaData.fields as field>
		<#if form.entity??>
			<@input.render field form.hasWritePermission form.entity />
		<#else>
			<@input.render field form.hasWritePermission />
		</#if>
	</#list>
</form>

<@f.remoteValidationRules form />

<script src="<@resource_href "/js/molgenis-form-edit.js"/>"></script>