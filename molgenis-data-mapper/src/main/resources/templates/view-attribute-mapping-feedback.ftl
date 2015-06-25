<div id="algorithm-result-feedback-container">
	<table class="table table-bordered">
		<thead>
		<#if (sourceAttributeNames)?has_content>
			<#list sourceAttributeNames as sourceAttributeName>
				<th>Source: ${sourceAttributeName?html}</th>
			</#list>
		</#if>
			<th>Target: ${targetAttribute.name?html}</th>
		</thead>
		<tbody>
			<#list feedbackRows as feedbackRow>
				<tr>
					<#if (sourceAttributeNames)?has_content>
						<#list sourceAttributeNames as sourceAttributeName>
							<#if feedbackRow.sourceEntity.getString(sourceAttributeName)??>
								<td>${feedbackRow.sourceEntity.getString(sourceAttributeName)?html}</td>
							</#if>
						</#list>
					</#if>
					<#if feedbackRow.success>
						<#if feedbackRow.value??>
							<#if feedbackRow.value?is_date_like>
								<td>${feedbackRow.value?datetime}</td>
							<#else>
								<td>${feedbackRow.value?html}</td>
							</#if>
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