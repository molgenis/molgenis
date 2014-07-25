<#-- modal for single entity data -->
<div class="modal hide large" id="entityReportModal" tabindex="-1" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">	
		
			<#-- modal header -->			
	      	<div class="modal-header">
	        	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	        	<h4 class="modal-title">Dataset: ${entityName}</h4>
	     	</div>
	     	
	     	<#-- modal body -->
	      	<div class="modal-body">
	      		<div class="control-group form-horizontal">					

					<#-- Specific content is imported here -->
					<div class="specific-content">
						<#attempt>
	   						<#include "view-specific-"+entityName+".ftl">		
	    					
	    					<#-- If specific content is not present then recover is activated and standard entity report is loaded -->
	    					<#recover>
	    					
	    					<#assign counter = 0 />
			    			<#-- Generic entity information split in rows of three -->
							<table class="table">
								<tbody>
									<tr>
										<#list entityMap?keys as key>
											<#if counter == 3>
												</tr>
												<tr>
												<#assign counter = 0>
											</#if>
											
											<th>${key}</th>
											<td>${entityMap[key]}</td>
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