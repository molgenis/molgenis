<table class="table table-striped table-bordered table-hover table-condensed">
	<thead>
		<tr>
	<#if workflowStep.inputFeatures?has_content>
		<#list workflowStep.inputFeatures as inputFeature>
			<th class="input-feature">${inputFeature.name}</th>
		</#list>
	</#if>
			<th></th>
	<#list workflowStep.features as feature>
			<th>${feature.name}</th>
	</#list>
		</tr>
	</thead>
	<tbody>
	<#if workflowStep.inputFeatures?has_content>
		<#list workflowStepData.dataMatrix as dataRow>
			<#if dataRow.linkedDataRows?size == 0>
			<tr>
				<#list dataRow.values as col>
				<td rowspan="${dataRow.linkedDataRows?size}">${col}<a href="#"><img class="pull-right" src="/img/new.png"></a></td>
				</#list>
			</tr>
			<#else>
				<#list dataRow.linkedDataRows as linkedDataRow>
			<tr>
					<#if linkedDataRow_index == 0>
						<#list dataRow.values as col>
				<td rowspan="${dataRow.linkedDataRows?size}">${col}<a href="#"><img class="pull-right" src="/img/new.png"></a></td>
						</#list>
					</#if>
				
				<td class="snug"><a href="#"><img src="/img/delete.png"></a></td>
					<#list linkedDataRow.values as col>
						<#if col??>
							<#if col?is_sequence || col?is_boolean>
				<td>BLAAT</td>
							<#else>
				<td>${col}</td>
							</#if>
							<#else>
				<td>null</td>
							</#if>
						</#list>
			</tr>
				</#list>
			</#if>
		</#list>
	<#else>
		<#list workflowStepData.dataMatrix as dataRow>
			<tr>
				<td class="snug"><a href="#"><img src="/img/delete.png"></a></td>
			<#list dataRow.values as col>
				<#if col??>
					<#if col?is_sequence || col?is_boolean>
				<td>BLAAT</td>
					<#else>
				<td>${col}</td>
					</#if>
				<#else>
				<td>null</td>
				</#if>
			</#list>
			</tr>
		</#list>
	</#if>
	</tbody>
</table>
<div class="row-fluid">
<#if !workflowStep.inputFeatures?has_content>
	<a href="#"><img src="/img/new.png"></a>
</#if>
</div>