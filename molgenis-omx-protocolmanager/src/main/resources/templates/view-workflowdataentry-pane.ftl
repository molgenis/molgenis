<table class="table table-striped table-bordered table-hover table-condensed">
	<thead>
		<tr>
<#assign features = []>
<#assign connectedFeatures = []>
<#if workflowElement.elementConnections?has_content>
	<#list workflowElement.elementConnections as elementConnection>
		<#assign connectedFeatures = connectedFeatures + [elementConnection.outputFeature] />
			<th class="input-feature">${elementConnection.outputFeature.name}</th>
	</#list>
</#if>
			<th></th><#-- delete column -->
<#list workflowElement.features as feature>
	<#if !connectedFeatures?seq_contains(feature)>
		<#assign features = features + [feature]>
			<th>${feature.name}</th>
	</#if>
</#list>
<#assign allFeatures = connectedFeatures + features>
		</tr>
	</thead>
	<tbody>		
<#-- case: connected features -->
<#if connectedFeatures?has_content>
	<#list workflowElement.elementConnections as elementConnection>
		<#assign inputFeature = elementConnection.inputFeature.id>
		<#assign inputDataRows = elementConnection.inputElement.workflowElementData.elementDataRows>
		<#list inputDataRows as inputDataRow>
			<#if inputDataRow.outgoingElementDataRowConnections?has_content>
				<#list inputDataRow.outgoingElementDataRowConnections as elementDataRowConnection>
			<tr>
					<#assign outputDataRow = elementDataRowConnection.outputDataRow>
					<#if elementDataRowConnection_index == 0>
						<#list connectedFeatures as feature>
							<#if outputDataRow.getValue(feature.id)??>
								<#assign value = outputDataRow.getValue(feature.id)>
								<#if value?is_sequence || value?is_boolean>
				<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">FIXME support sequence and boolean types</td>
								<#else>
				<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">${value}<a href="#"><img class="pull-right" src="/img/new.png"></a></td>
								</#if>
							<#else>
							</#if>
						</#list>
					</#if>
				<td class="snug"><a href="#"><img src="/img/delete.png"></a></td>
					<#list features as feature>
						<#if outputDataRow.getValue(feature.id)??>
							<#assign value = outputDataRow.getValue(feature.id)>
							<#if value?is_sequence || value?is_boolean>
				<td>FIXME support sequence and boolean types</td>
							<#else>
				<td>${value}</td>
							</#if>
						<#else>
				<td></td>
						</#if>
					</#list>
			</tr>
				</#list>
			<#else>
			<tr>
				<#assign value = inputDataRow.getValue(inputFeature)>
				<#if value?is_sequence || value?is_boolean>
				<td>FIXME support sequence and boolean types</td>
				<#else>
				<td>${value}<a href="#"><img class="pull-right" src="/img/new.png"></a></td>
				</#if>
			</tr>
			</#if>
		</tr>
		</#list>
	</#list>
<#else>
<#-- case: no connected features -->
	<#list workflowElement.workflowElementData.elementDataRows as dataRow>
			<tr>
				<td class="snug"><a href="#"><img src="/img/delete.png"></a></td>
		<#list allFeatures as feature>
			<#if dataRow.getValue(feature.id)??>
				<#assign value = dataRow.getValue(feature.id)>
				<#if value?is_sequence || value?is_boolean>
				<td>FIXME support sequence and boolean types</td>
				<#else>
				<td>${value}</td>
				</#if>
			<#else>
				<td></td>
			</#if>
		</#list>
			</tr>
	</#list>
</#if>
	</tbody>
</table>
<div class="row-fluid">
<#if !connectedFeatures?has_content>
	<a href="#"><img src="/img/new.png"></a>
</#if>
</div>

<#--		
		
		
		
		
	<#if workflowElement.inputFeatures?has_content>
		<#list workflowElement.inputFeatures as inputFeature>
			<th class="input-feature">${inputFeature.name}</th>
		</#list>
	</#if>
			<th></th>
	<#list workflowElement.features as feature>
			<th>${feature.name}</th>
	</#list>
		</tr>
	</thead>
	<tbody>
	<#if workflowElement.inputFeatures?has_content>
		<#list workflowElementData.dataMatrix as dataRow>
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
		<#list workflowElementData.dataMatrix as dataRow>
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
<#if !workflowElement.inputFeatures?has_content>
	<a href="#"><img src="/img/new.png"></a>
</#if>
</div>
-->