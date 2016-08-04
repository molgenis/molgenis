<div id="algorithm-result-feedback-container">
	<div style="overflow-x: auto">
    	<table class="table table-bordered">
    		<thead>
                    <th></th>
    		<#if (sourceAttributes)?has_content>
    			<#list sourceAttributes as sourceAttribute>
    				<th>Source: ${sourceAttribute.name?html}</th>
    			</#list>
    		</#if>
    			<th>Target: ${targetAttribute.name?html}</th>
    		</thead>
    		<tbody>
    			<#list feedbackRows as feedbackRow>
    				<tr>
    				    <#-- Dataexplorer can't be initialized with query at the moment, for forward compatibility already construct URL -->
    				    <td><a class="btn btn-default btn-xs" href="javascript:window.location='${dataexplorerUri?html}?entity=${source?html}&q=' + molgenis.createRsqlQuery([{field: '${feedbackRow.sourceEntity.getEntityMetaData().getIdAttribute().getName()?html}', operator: 'EQUALS', value: '${feedbackRow.sourceEntity.getIdValue()?string?html}' }]);" role="button"><span class="glyphicon glyphicon-search"></span></a></td>
    					<#if (sourceAttributes)?has_content>
    						<#list sourceAttributes as sourceAttribute>
								<#if sourceAttribute.dataType == "XREF" || sourceAttribute.dataType == "CATEGORICAL">
									<#if feedbackRow.sourceEntity.get(sourceAttribute.name)??>
	    								<td>
	    									<#assign refEntity = feedbackRow.sourceEntity.get(sourceAttribute.name)>
	    									<#assign refEntityMetaData = sourceAttribute.refEntity>
											<#list refEntityMetaData.attributes as refAttribute>
												<#assign refAttributeName = refAttribute.name>
                                                ${refEntity[refAttributeName]} <#if refAttribute?has_next>=</#if>
											</#list> 
	    								</td>
	    							</#if>
								<#else>
									<#if feedbackRow.sourceEntity.get(sourceAttribute.name)??>
										<#assign value = feedbackRow.sourceEntity.get(sourceAttribute.name)>
                                       	<#if value?is_sequence> <!-- its mref values -->
                                        	<td>
												<#list value as row>
													${row.labelValue?html}<#if row?has_next>, </#if>
												</#list>
                                        	</td>
										<#elseif value?is_boolean>
											<td>${value?c}</td>
										<#else>
											<td>${value?html}</td>
										</#if>
	    							</#if>
								</#if>
    						</#list>
    					</#if>
    					<#if feedbackRow.success>
    						<#if feedbackRow.value??>
								<#if feedbackRow.value?is_date_like> <!-- its a date or datetime -->
									<td>${feedbackRow.value?datetime}</td>
								<#elseif feedbackRow.value?is_hash> <!-- its an entity -->
                                    <td>${feedbackRow.value.getLabelValue()?html}</td>
								<#elseif feedbackRow.value?is_sequence> <!-- its mref values -->
									<td>
										<#list feedbackRow.value as row>
											${row.labelValue?html}<#if row?has_next>, </#if>
										</#list>
									</td>
								<#else> <!-- its string or int value -->
									<td>${feedbackRow.value?html}</td>
								</#if>
							<#else>
								<td><i>null</i></td>
							</#if>
    					<#else>
    						<td>
    							<span class="label label-danger">
    								Invalid script
    							</span>
    						</td>
    					</#if>
    				</tr>
    			</#list>
    		</tbody>
    	</table>
	</div>
</div>