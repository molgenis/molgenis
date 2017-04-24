<h2>Dataset: ${entityType.getLabel()?html}</h2>

<#assign counter = 0 />
<table class="table">
    <tbody>
    <tr>
    <#list entity.getEntityType().getAtomicAttributes() as atomicAttribute>
        <#assign key = atomicAttribute.getName()>

        <#if counter == 3>
        </tr>
        <tr>
            <#assign counter = 0>
        </#if>

        <th>${key?html}</th>
        <#if entity.get(key)??>
            <#assign type = atomicAttribute.getDataType()>
            <td><#if type == "CATEGORICAL_MREF" || type == "MREF" || type == "ONE_TO_MANY"><#list entity.getEntities(key) as entity>${entity.getLabelValue()!?html}<#sep>
                , </#sep></#list>
            <#elseif type == "CATEGORICAL" || type == "FILE" || type == "XREF"><#if entity.getEntity(key)??>${entity.getEntity(key).getLabelValue()!?html}</#if>
            <#elseif type == "BOOL">${entity.getBoolean(key)?c}
            <#elseif type == "DATE" || type == "DATE_TIME">${entity.get(key)?datetime}
            <#else>${entity.get(key)!?html}</#if></td>
        <#else>
            <td>&nbsp;</td>
        </#if>

        <#assign counter = counter + 1>
    </#list>

    <#-- fill last row with empty data -->
    <#assign counter = 3 - counter>
    <#list 1..counter as i>
        <th>&nbsp;</th>
        <td>&nbsp;</td>
    </#list>
    </tr>
    </tbody>
</table>