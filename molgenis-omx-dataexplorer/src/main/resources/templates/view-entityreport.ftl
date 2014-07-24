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
	    			
	    			<#-- Generic entity information -->
					<dl class="dl-horizontal">
						<#list entityMap?keys as key>
							<#if entityMap[key] != entityId>
								<dt>${key}</dt>
								<dd>${entityMap[key]}</dd>
							</#if>
						</#list>
					</dl>

					<#-- Specific content is imported here -->
					<div class="specific-content">
						<#attempt>
	   						<#include "view-specific-"+entityName+".ftl">
	    					<#recover>
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