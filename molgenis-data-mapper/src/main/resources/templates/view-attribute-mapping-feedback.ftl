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
								<#if sourceAttribute.dataType == "xref" || sourceAttribute.dataType == "categorical">
									<#if feedbackRow.sourceEntity.get(sourceAttribute.name)??>
	    								<td>
	    									<#assign refEntity = feedbackRow.sourceEntity.get(sourceAttribute.name)>
	    									<#assign refEntityMetaData = sourceAttribute.refEntity>
											<#list refEntityMetaData.attributes as refAttribute>
												<#assign refAttributeName = refAttribute.name>
												<#if (refEntity[refAttributeName])??>
													<#assign value = refEntity[refAttributeName]>
													<#if value?is_boolean>${value?c}<#else>${value}</#if><#if refAttribute?has_next>=</#if>
												</#if>
											</#list>
	    								</td>
	    							</#if>
								<#elseif sourceAttribute.dataType == "mref">
									<#if feedbackRow.sourceEntity.get(sourceAttribute.name)??>
                                    	<td>
											<#assign refEntity = feedbackRow.sourceEntity.get(sourceAttribute.name)>
											<#assign refEntityMetaData = sourceAttribute.refEntity>
                                            <#list refEntity as entity>
                                                ${entity.getIdValue()}
                                            </#list>
										</td>
									</#if>
								<#else>
									<#if feedbackRow.sourceEntity.getString(sourceAttribute.name)??>
	    								<td>${feedbackRow.sourceEntity.getString(sourceAttribute.name)?html}</td>
	    							</#if>
								</#if>
    						</#list>
    					</#if>
    					<#if feedbackRow.success>
    						<#if feedbackRow.value??>
    							<#if feedbackRow.value?is_date_like>
    								<td>${feedbackRow.value?datetime}</td>
    							<#elseif feedbackRow.value?is_sequence>
									<td><#list feedbackRow.value as value>
									    <#if value?has_content>${value?html}</#if>
									</#list>
								</td>
								<#else>
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