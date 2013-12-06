<table class="table table-hover table-condensed">
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
			<#if inputDataRow.completed>
				<#-- case: input data row has outgoing connections -->
				<#if inputDataRow.outgoingElementDataRowConnections?has_content>
					<#list inputDataRow.outgoingElementDataRowConnections as outgoingDataRowConnection>
						<#assign outputDataRow = outgoingDataRowConnection.outputDataRow>
						<#if !processedOutputDataRows?seq_contains(outputDataRow)>
		<tr data-datarow="${outputDataRow.id?c}" data-input-rows="<#list outputDataRow.incomingElementDataRowConnections as incomingElementDataRowConnection><#if incomingElementDataRowConnection_index &gt; 0>,</#if>${incomingElementDataRowConnection.inputDataRow.id?c}</#list>">		
							<#if outgoingDataRowConnection_index == 0>
								<#list connectedFeatures as feature>
			<td data-feature="${feature.id?c}" rowspan="${inputDataRow.outgoingElementDataRowConnections?size}">
									<#if outputDataRow.getValue(feature.id)??><@createInput feature.dataType outputDataRow.getValue(feature.id) true /><#else><@createInput feature.dataType "" true /></#if>
										<#if outputFeature.dataType?upper_case != "MREF">
				<a href="#" class="create-row-btn"><img class="pull-right" src="/img/new.png"></a>
										</#if>
			</td>
								</#list>
							</#if>
							<#assign processedOutputDataRows = processedOutputDataRows + [outputDataRow] />
			<td class="snug">
				<a href="#" class="delete-row-btn" data-row-id="${outputDataRow.id?c}"><img src="/img/delete.png"></a>
			</td>
							<#list features as feature>
			<td<#if feature.required> class="required-feature"</#if> data-feature="${feature.id?c}">
								<#if outputDataRow.getValue(feature.id)??><@createInput feature.dataType outputDataRow.getValue(feature.id) false /><#else><@createInput feature.dataType "" false /></#if>
			</td>
							</#list>
		</tr>
						</#if>
					</#list>
				<#-- case: input data row does not have outgoing connections -->
				<#else>
		<tr data-input-rows="${inputDataRow.id?c}">
			<td data-feature="${outputFeature.id?c}">
					<#if outputFeature.dataType?upper_case == "MREF">
				<label class="checkbox"><input type="checkbox">
					</#if>
					<#if inputDataRow.getValue(inputFeature.id)??><@createInput outputFeature.dataType inputDataRow.getValue(inputFeature.id) true /><#else><@createInput outputFeature.dataType "" true /></#if>
					<#if outputFeature.dataType?upper_case == "MREF">
				</label>
					<#else>
				<a href="#" class="create-row-btn"><img class="pull-right" src="/img/new.png"></a>
					</#if>
			</td>
		</tr>
				</#if>
			</#if>
		</#list>
	</#list>
<#-- case: no connected features -->
<#else>
	<#list workflowElement.workflowElementData.elementDataRows as dataRow>
			<tr data-datarow="${dataRow.id?c}">
				<td class="snug"><a href="#" class="delete-row-btn" data-row-id="${dataRow.id?c}"><img src="/img/delete.png"></a></td>
		<#list allFeatures as feature>
				<td<#if feature.required> class="required-feature"</#if> data-feature="${feature.id?c}">
			<#if dataRow.getValue(feature.id)??><@createInput feature.dataType dataRow.getValue(feature.id) false /><#else><@createInput feature.dataType "" false /></#if>
				</td>
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
	<button class="btn combine-rows-btn" type="button">Combine selected</button>
			<#break>
		</#if>
	</#list>
<#else>
	<a href="#" class="create-row-btn"><img src="/img/new.png"></a>
</#if>
</div>
<#macro createInput type value readonly>
	<#if type?upper_case == 'BOOL'>
		<input type="checkbox"<#if value??> checked</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'STRING'>
		<input type="text"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'XREF'>
		<input type="text"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'MREF'>
		<input type="text"<#if value??> value="<#if value?is_sequence><#list value as subValue><#if subValue_index &gt; 0>, </#if>${subValue}</#list><#else>${value}</#if>"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'DATE'>
		<input type="date"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'INT'>
		<input type="int"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'CATEGORICAL'>
		<select><option <#if value??> value="${value}"</#if><#if readonly> disabled</#if>></option></select>
	<#elseif type?upper_case == 'DATETIME'>
		<input type="datetime"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'DECIMAL'>
		<input type="number" step="any"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'EMAIL'>
		<input type="email"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'ENUM'>
		<select><option <#if value??> value="${value}"</#if><#if readonly> disabled</#if>></option></select>
	<#elseif type?upper_case == 'FILE'>
		<input type="file"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'HTML'>
		<input type="text"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'HYPERLINK'>
		<input type="url"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'IMAGE'>
		<input type="image"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'LONG'>
		<input type="number" min="0" step="1" pattern="\d+"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#elseif type?upper_case == 'TEXT'>
		<input type="text"<#if value??> value="${value}"</#if><#if readonly> disabled</#if>>
	<#else>
		<span>ERROR: type ${type} not supported</span>
	</#if>
</#macro>