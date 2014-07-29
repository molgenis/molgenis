<#-- modal for single entity data -->
<div class="modal hide large" id="entityReportModal" tabindex="-1" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">	
		
			<#-- modal header -->			
	      	<div class="modal-header">
	        	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	        	<h4 class="modal-title">Dataset: ${entity.getEntityMetaData().getName()}</h4>
	        	
	     	</div>
	     	
	     	<#-- modal body -->
	      	<div class="modal-body">
	      		<div class="control-group form-horizontal">					

					<#-- Specific content is imported here -->
					<div class="specific-content">
						<#attempt>
	   						<#include "view-specific-"+entity.getEntityMetaData().getName()+".ftl">		
	    					
	    					<#-- If specific content is not present then recover is activated and standard entity report is loaded -->
	    					<#recover>
	    					
	    					<#assign counter = 0 />
			    			
			    			<#-- Generic entity information split in rows of three -->
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
									</tr>
								</tbody>
							</table>	
							
						</#attempt>
					</div>					
	      		</div>
			</div>
			
			<#-- modal footer -->
	      	<div class="modal-footer">
	        	<button type="button" class="btn btn-default" data-dismiss="modal">close</button>
	      	</div>
	    </div>
	</div>
</div>