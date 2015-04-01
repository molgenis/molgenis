<#include "resource-macros.ftl">
<#import "molgenis-input-elements.ftl" as input>
<#import "form-macros.ftl" as f>

<form class="form-horizontal" data-id="${entityName?html}" id="entity-form" method="POST" action="/api/v1/${entityName?html}">
	<#list form.metaData.fields as field>
		<#if form.entity??>
			<@input.render field form.hasWritePermission form.entity form.metaData.forUpdate/>
		<#else>
			<@input.render field form.hasWritePermission '' form.metaData.forUpdate/>
		</#if>
	</#list>
</form>

<@f.remoteValidationRules form />

<script src="<@resource_href "/js/molgenis-form-edit.js"/>"></script>
<script src="<@resource_href "/js/molgenis-script-evaluator.js"/>"></script>