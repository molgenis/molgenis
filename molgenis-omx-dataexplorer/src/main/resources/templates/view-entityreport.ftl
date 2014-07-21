<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
	"jquery.bootstrap.wizard.css",
	"bootstrap-datetimepicker.min.css",
	"ui.fancytree.min.css",
	"jquery-ui-1.9.2.custom.min.css",
	"select2.css",
	"iThing-min.css",
	"bootstrap-switch.min.css",
	"dataexplorer.css",
	"diseasematcher.css"]>
<#assign js=[
	"jquery-ui-1.9.2.custom.min.js",
	"jquery.bootstrap.wizard.min.js",
	"bootstrap-datetimepicker.min.js",
	"dataexplorer-filter.js",
	"dataexplorer-filter-dialog.js",
	"dataexplorer-filter-wizard.js",
	"jquery.fancytree.min.js",
	"jquery.molgenis.tree.js",
	"select2.min.js",
	"jQEditRangeSlider-min.js",
	"bootstrap-switch.min.js",
	"jquery.molgenis.xrefsearch.js",
	"dataexplorer.js",
	"jquery.molgenis.table.js"]>

<@header css js/>

<#-- modal for single entity data -->
<div class="modal large" id="entityReportModal" tabindex="-1" aria-hidden="true">
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

<@footer/>