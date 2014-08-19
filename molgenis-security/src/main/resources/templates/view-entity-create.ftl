<#include "resource-macros.ftl">
<#import "molgenis-input-elements.ftl" as input>
<#import "form-macros.ftl" as f>

<form class="form-horizontal" id="entity-form" method="POST" action="/api/v1/${entityName}">
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