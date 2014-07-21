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
	    			
	    				
    				<#-- table showing single entity data -->
    				<table class="table table-responsive" style="margin:auto;">
						<caption>Entity information</caption>
						<thead>
							<#list entityMap?keys as key>
								<#if entityMap[key] != entityId>
									<th>${key}</th>
								</#if>
							</#list>		
						</thead>
						<tbody>
							<tr>	
								<#list entityMap?keys as key>
									<#if entityMap[key] != entityId>
										<td>${entityMap[key]}</td>
									</#if>
								</#list>
							</tr>			
						</tbody>	
					</table>
					
					<#if parameterMap??>
						<br />
						<hr></hr>
						<br />
							
						<#-- div for optional content in parameter map, like images -->	
						<div>
						
							<#-- Idea is that we scan keys for generic html elements like img or button -->
							<#-- based on values like file location we can then for example make an image -->
							<#list parameterMap?keys as key>
								${key} : ${parameterMap[key]} <br />
							</#list>
						
						</div>
					</#if>
				
	      		</div>
			</div>
			
			<#-- modal footer -->
	      	<div class="modal-footer">
	        	<button type="button" class="btn btn-default" data-dismiss="modal">close</button>
	      	</div>
	    </div>
	</div>
</div>