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
                <td><a class="btn btn-default btn-xs"
                       href="javascript:window.location='${dataexplorerUri?html}?entity=${source?html}&q=' + molgenis.createRsqlQuery([{field: '${feedbackRow.sourceEntity.getEntityType().getIdAttribute().getName()?html}', operator: 'EQUALS', value: '${feedbackRow.sourceEntity.getIdValue()?string?html}' }]);"
                       role="button"><span class="glyphicon glyphicon-search"></span></a></td>
                <#if (sourceAttributes)?has_content>
                    <#list sourceAttributes as sourceAttribute>
                        <#if sourceAttribute.dataType == "XREF" || sourceAttribute.dataType == "CATEGORICAL">
                            <#if feedbackRow.sourceEntity.get(sourceAttribute.name)??>
                                <td>
                                    <#assign refEntity = feedbackRow.sourceEntity.get(sourceAttribute.name)>
                                    <#assign refEntityType = sourceAttribute.refEntity>
                                    <#list refEntityType.attributes as refAttribute>
                                        <#assign refAttributeName = refAttribute.name>
                                        <#if (refEntity[refAttributeName])??>
                                            <#assign value = refEntity[refAttributeName]>
                                            <#if value?is_boolean>${value?c}<#else>${value}</#if><#if refAttribute?has_next>
                                            = </#if>
                                        </#if>
                                    </#list>

                                </td>
                            </#if>
                        <#elseif sourceAttribute.dataType == "mref">
                            <#if feedbackRow.sourceEntity.get(sourceAttribute.name)??>
                                <td>
                                    <#assign refEntity = feedbackRow.sourceEntity.get(sourceAttribute.name)>
											<#assign refEntityType = sourceAttribute.refEntity>
                                            <#list refEntity as entity>
                                ${entity.getIdValue()}
                                </#list>
                                </td>
                            </#if>
                        <#elseif sourceAttribute.dataType == "DATE">
                            <#if feedbackRow.sourceEntity.get(sourceAttribute.name)??>
                                <td>${feedbackRow.sourceEntity.get(sourceAttribute.name).format('MMM d, yyyy')}</td>
                            </#if>
                        <#elseif sourceAttribute.dataType == "DATE_TIME">
                            <#if feedbackRow.sourceEntity.get(sourceAttribute.name)??>
                                <td>${feedbackRow.sourceEntity.get(sourceAttribute.name).format('MMM d, yyyy HH:mm:SS a')}</td>
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
                            <#else>
                                <td></td>
                            </#if>
                        </#if>
                    </#list>
                </#if>
                <#if feedbackRow.success>
                    <#if feedbackRow.value??>
                        <#if targetAttribute.dataType == 'DATE'> <!-- its a date -->
                            <td>${feedbackRow.value.format('MMM d, yyyy')}</td>
                        <#elseif targetAttribute.dataType == 'DATE_TIME'> <!-- its a date_time -->
                            <td>${feedbackRow.value.format('MMM d, yyyy HH:mm:SS a')}</td>
                        <#elseif feedbackRow.value?is_hash> <!-- its an entity -->
                            <td>${feedbackRow.value.getLabelValue()?html}</td>
                        <#elseif feedbackRow.value?is_sequence> <!-- its mref values -->
                            <td>
                                <#list feedbackRow.value as row>
                                    <#if row?has_content>${row.labelValue?html}<#if row?has_next>, </#if></#if>
                                </#list>
                            </td>
                        <#elseif feedbackRow.value?is_boolean> <!-- its a boolean -->
                            <td>${feedbackRow.value?c}</td>
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