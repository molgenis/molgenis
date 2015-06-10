<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['']>
<#assign js=['attribute-mapping-feedback.js']>

<@header css js/>

<div class="row">
	<div class="col-md-12">
		<a href="${context_url}/mappingproject/${mappingProject.identifier}" class="btn btn-default">
			<span class="glyphicon glyphicon-chevron-left"></span> Back to project
		</a>
		
		<hr></hr>
	</div>
</div>

<div class="row">
	<div class="col-md-6">		
		<table class="table">
			<thead>
			<#list sourceAttributeNames as sourceAttributeName>
				<th>${sourceAttributeName?html}</th>
			</#list>
				<th>${targetAttribute.name?html}</th>
			</thead>
			<tbody>
				<#list feedbackRows as feedbackRow>
					<tr>
						<#list sourceAttributeNames as sourceAttributeName>
							<td>${feedbackRow.sourceEntity.getString(sourceAttributeName)?html}</td>
						</#list>
						<#if feedbackRow.success>
							<#if feedbackRow.value??>
								<td>${feedbackRow.value?html}</td>
							<#else>	
								<td><i>null</i></td>
							</#if>
						<#else>
							<td>
								<button class="btn btn-sm btn-danger show-error-message" data-message="${feedbackRow.exception.message!""?html}">
									Error, click for more details
								</button>
							</td>
						</#if>
					</tr>
				</#list>
			</tbody>
		</table>
	</div>
	<div class="col-md-6">
		<div id="algorithm-error-message-container"></div>
	</div>
</div>

<@footer />