<#-- modal for single entity data -->
<div class="modal hide large" id="entityReportModal" tabindex="-1" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">	
			<#attempt>
				<#include "view-specific-"+entity.getEntityMetaData().getName()+".ftl">		
				
				<#-- If specific content is not present then recover is activated and standard entity report is loaded -->
				<#recover>
				
				<#assign counter = 0 />
				<#-- Generic entity information split in rows of three -->
				
				<#-- modal header -->			
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					<h4 class="modal-title">DataSet: ${entity.getEntityMetaData().getName()}</h4>
				</div>
				
				<div class="modal-body">
					<div class="control-group form-horizontal">
				
						<table class="table">
							<tbody>
								<tr>
									<#list entity.getAttributeNames() as key>
										
										<#if counter == 3>
											</tr>
											<tr>
											<#assign counter = 0>
										</#if>
											
										<th>${key}</th>
										<#if entity.get(key)??>
											<#if entity.get(key)?is_sequence>
												<td>
												<#list entity.get(key) as value>
													${value}<#if value_has_next>, </#if>
												</#list>
												</td>
											<#else>
												<td>${entity.getString(key)}</td>
											</#if>
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
			</#attempt>
	    </div>
	</div>
</div>