<#-- modal header -->			
<div class="modal-header">
	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	<h4 class="modal-title">DataSet: ${entityMetadata.getLabel()?html}</h4>
</div>

<#-- modal body -->
<div class="modal-body">
	<div class="control-group form-horizontal">
	<#-- Generic entity information split into three columns -->
		<#assign counter = 0 />
		<table class="table">
			<tbody>
				<tr>
					<#list entity.getEntityMetaData().getAtomicAttributes() as atomicAttribute>
                        <#assign key = atomicAttribute.getName()>

						<#if counter == 3>
							</tr>
							<tr>
							<#assign counter = 0>
						</#if>
							
						<th>${key?html}</th>
						<#if entity.get(key)??>
							<#assign type=atomicAttribute.getDataType().getEnumType()>
                            <td><#if type == "CATEGORICAL_MREF" || type == "MREF"><#list entity.getEntities(key) as entity>${entity.getLabelValue()!?html}<#sep>, </#sep></#list>
                            <#elseif type == "CATEGORICAL" || type == "FILE" || type == "XREF"><#if entity.getEntity(key)??>${entity.getEntity(key).getLabelValue()!?html}</#if>
                            <#else>${entity.getString(key)!?html}</#if></td>
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
	</div>
</div>

<#-- modal footer -->
<div class="modal-footer">
	<button type="button" class="btn btn-default" data-dismiss="modal">close</button>
</div>