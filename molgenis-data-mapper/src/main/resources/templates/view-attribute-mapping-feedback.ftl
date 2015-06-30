<div id="algorithm-result-feedback-container">
	<form>
		<div class="form-group">
			<div class="input-group">
  				<span class="input-group-btn">
    				<button id="result-search-btn" class="btn btn-default" type="button"><span class="glyphicon glyphicon-search"></button>
  				</span>
  				<input id="result-search-field" type="text" class="form-control" placeholder="Search...">
			</div>
		</div>
	</form>
	
	<form class="form-inline">
		<div class="form-group">
			<div class="checkbox">
				<label>
  					<input id="errors-only-checkbox" type="checkbox"> Errors only
				</label>
			</div>
		</div>	
		<div class="form-group  pull-right">
			<strong>Success: ${success}, Missing: ${missing}, Error: ${error}</strong></p>
		</div>
	</form>
	
	<br/>
	<div style="overflow-x: auto">
	<table class="table table-bordered">
		<thead>
                <th></th>
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
				    <#-- Dataexplorer can't be initialized with query at the moment, for forward compatibility already construct URL -->
				    <td><a class="btn btn-default btn-xs" href="javascript:window.location='${dataexplorerUri?html}?entity=${source?html}&q=' + molgenis.createRsqlQuery([{field: '${feedbackRow.sourceEntity.getEntityMetaData().getIdAttribute().getName()?html}', operator: 'EQUALS', value: '${feedbackRow.sourceEntity.getIdValue()?string?html}' }]);" role="button"><span class="glyphicon glyphicon-search"></span></a></td>
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
</div>