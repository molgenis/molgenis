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
			<th></th><#-- delete icon column -->
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
	<#assign processedOutputDataRows = []>
	<#list workflowElement.elementConnections as elementConnection>
		<#assign inputFeature = elementConnection.inputFeature>
		<#assign outputFeature = elementConnection.outputFeature>
		<#assign inputDataRows = elementConnection.inputElement.workflowElementData.elementDataRows>
		<#list inputDataRows as inputDataRow>
			<#if inputDataRow.outgoingElementDataRowConnections?has_content>
				<#list inputDataRow.outgoingElementDataRowConnections as outgoingDataRowConnection>
					<#assign outputDataRow = outgoingDataRowConnection.outputDataRow>
					<#if !processedOutputDataRows?seq_contains(outputDataRow)>
		<tr>		
						<#if outgoingDataRowConnection_index == 0>
							<#list connectedFeatures as feature>
								<#if feature.dataType == "mref">
									<#if outputDataRow.getValue(feature.id)??>
										<#assign value = outputDataRow.getValue(feature.id)>
										<#if value?is_sequence>
			<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">
				<label class="checkbox"><input type="checkbox"> 
											<#list value as subValue><#if subValue_index &gt; 0>, </#if>${subValue}</#list>
											<#if value?size &gt; 1><button class="btn btn-mini pull-right" type="button">Split</button></#if>
				</label>
			</td>						
										<#elseif value?is_boolean>
			<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">FIXME support boolean type</td>
										<#else>
			<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">${value}<a href="#"><img class="pull-right" src="/img/new.png"></a></td>
										</#if>
									<#else>
									</#if>
								<#else>
									<#if outputDataRow.getValue(feature.id)??>
										<#assign value = outputDataRow.getValue(feature.id)>
										<#if value?is_sequence>
			<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">
											<#list value as subValue><#if subValue_index &gt; 0>,</#if>${subValue}</#list>
			</td>						
										<#elseif value?is_boolean>
			<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">FIXME support boolean type</td>
										<#else>
			<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">${value}<a href="#"><img class="pull-right" src="/img/new.png"></a></td>
										</#if>
									<#else>
									</#if>
								</#if>
							</#list>
						</#if>
						<#assign processedOutputDataRows = processedOutputDataRows + [outputDataRow] />
			<td class="snug"><a href="#"><img src="/img/delete.png"></a></td>
						<#list features as feature>
							<#if outputDataRow.getValue(feature.id)??>
								<#assign value = outputDataRow.getValue(feature.id)>
								<#if value?is_sequence>
			<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">
									<#list value as subValue><#if subValue_index &gt; 0>,</#if>${subValue}</#list>
			</td>						
								<#elseif value?is_boolean>
			<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">FIXME support boolean type</td>
								<#else>
			<td>${value}</td>
								</#if>
							<#else>
			<td></td>
							</#if>
						</#list>
		</tr>
					</#if>
				</#list>
			<#else>
		<tr>
				<#assign value = inputDataRow.getValue(inputFeature.id)>
				<#if value?is_sequence>
			<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">
					<#list value as subValue><#if subValue_index &gt; 0>,</#if>${subValue}</#list>
			</td>						
				<#elseif value?is_boolean>
			<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">FIXME support boolean type</td>
				<#else>
					<#if outputFeature.dataType == "mref">
			<td><label class="checkbox"><input type="checkbox"> ${value}</label></td>	
					<#else>
			<td>${value}<a href="#"><img class="pull-right" src="/img/new.png"></a></td>
					</#if>
				</#if>
		</tr>
			</#if>
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
				<#if value?is_sequence>
			<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">
					<#list value as subValue><#if subValue_index &gt; 0>,</#if>${subValue}</#list>
			</td>						
				<#elseif value?is_boolean>
			<td rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">FIXME support boolean type</td>
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
<#if connectedFeatures?has_content>
	<#list workflowElement.elementConnections as elementConnection>
		<#if elementConnection.outputFeature.dataType == "mref">
	<button class="btn" type="button">Combine selected</button>
			<#break>
		</#if>
	</#list>
<#else>
	<a href="#"><img src="/img/new.png"></a>
</#if>
</div>