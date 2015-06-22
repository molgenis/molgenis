<div id="algorithm-result-feedback-container">
	<strong>Success: ${success}, Missing: ${missing}, Error: ${error}</strong></p>
	<form class="form-inline">		
		<div class="form-group">
			<div class="checkbox">
				<label>
  					<input id="errors-only-checkbox" type="checkbox"> Errors only
				</label>
			</div>
		</div>	
		<div class="form-group pull-right">
			<div class="input-group">
  				<span class="input-group-btn">
    				<button id="result-search-btn" class="btn btn-default" type="button"><span class="glyphicon glyphicon-search"></button>
  				</span>
  				<input id="result-search-field" type="text" class="form-control" placeholder="Search">
			</div>
			<br></br>
		</div>
	</form>
	<br/>
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
							<td>${feedbackRow.sourceEntity.getString(sourceAttributeName)?html}</td>
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