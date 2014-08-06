<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['select2.css', 'bootstrap-datetimepicker.min.css', 'molgenis-form.css']>
<#assign js=['jquery.validate.min.js', 'select2.min.js']>

<@header css js/>

<div class="container-fluid">
	<div class="row-fluid">
		<h3>Scripts</h3>
		<table class="table">
			<thead>
				<tr>
					<th>Name</th>
					<th>Type</th>
					<th>Result file extension</th>
					<th>Parameters</th>
				</tr>
			</thead>
			<tbody>
			<#if scripts?has_content>
				<#list scripts as script>
				<tr>
					<td>${script.name!}</td>
					<td>${script.type!}</td>
					<td>${script.resultFileExtension!}</td>
					<td>
						<#if script.parameters?has_content>
							<#list script.parameters as parameter>
								${parameter}<#if parameter_has_next>,</#if>
							</#list>
						</#if>
					</td>
				</tr>
				</#list>
			</#if>
			</tbody>
		</table>
	</div>
</div>

<@footer/>