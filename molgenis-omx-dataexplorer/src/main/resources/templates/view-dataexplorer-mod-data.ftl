<div id="dataexplorer-grid-data">
	<div class="accordion" id="genomebrowser">
	    <div class="accordion-group">
	        <div class="accordion-heading">
	            <a class="accordion-toggle" data-toggle="collapse" href="#dalliance"><i class="icon-chevron-down"></i> Genome Browser</a>
	        </div>
	        <div id="dalliance" class="accordion-body collapse in">
	            <div class="accordion-inner">
	            	<#-- dalliance default id to print browser -->
	                <div id="svgHolder"></div>
	                <div class="pull-right"><a id="genomebrowser-filter-button" class="btn btn-small"><img src="/img/filter-bw.png"> Apply filter</a></div>
	            </div>
	        </div>
	    </div>
	</div>
	<div class="row-fluid data-table-container" id="data-table-container"></div>
		<div class="row-fluid data-table-pager-container">
			<div class="pull-right">
				<a id="download-modal-button" class="btn" data-toggle="modal" data-target="#downloadModal">Download as CSV</a>
			<#if galaxyEnabled?? && galaxyEnabled == true>
				<a id="galaxy-export-modal-button" class="btn" data-toggle="modal" data-target="#galaxy-export-modal">Export to Galaxy</a>
			</#if>
			</div>
		</div>
	</div>
</div>

<#-- CSV download modal -->
<div class="modal hide medium" id="downloadModal" tabindex="-1" role="dialog" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">				
	      	<div class="modal-header">
	        	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	        	<h4 class="modal-title">Download as csv</h4>
	     	</div>
	      	<div class="modal-body">
	      		<div class="control-group form-horizontal">
					<label class="control-label">As column names i want</label>
	    			<div class="controls">
	    				<label><input type="radio" name="ColNames" value ="ATTRIBUTE_LABELS" checked="true"> Attribute labels</label>
	    				<label><input type="radio" name="ColNames" value ="ATTRIBUTE_NAMES"> Attribute names</label>
					</div>
	      		</div>
			</div>
	      	<div class="modal-footer">
	        	<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	        	<button id="download-button" class="btn btn-primary">Download</button>
	      	</div>
	    </div>
	</div>
</div>
<#if galaxyEnabled?? && galaxyEnabled == true>
<#-- Galaxy export modal -->
<form name="galaxy-export-form" class="form-horizontal" action="${context_url}/galaxy/export" method="POST">				
	<div class="modal hide medium" id="galaxy-export-modal" tabindex="-1" role="dialog" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
		      	<div class="modal-header">
		        	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		        	<h4 class="modal-title">Export data set to Galaxy</h4>
		     	</div>
		      	<div class="modal-body">
		      		<div class="control-group">
			      		<label class="control-label" for="galaxy-export-url">Galaxy server URL *</label>
					    <div class="controls">
					    	<input type="text" id="galaxy-export-url" name="galaxyUrl" <#if galaxyUrl??>value="${galaxyUrl?html}" </#if>required><span class="help-block">e.g. https://usegalaxy.org/</span>
					    </div>
		      		</div>
		      		<div class="control-group">
		      			<label class="control-label" for="galaxy-export-api-key">Galaxy API key *</label>
					    <div class="controls">
					    	<input type="password" id="galaxy-export-api-key" name="galaxyApiKey" <#if galaxyApiKey??>value="${galaxyApiKey?html}" </#if>required><span class="help-block">See 'Select API Keys' in menu 'User' on e.g. https://usegalaxy.org/</span>
					    </div>
		      		</div>
				</div>
		      	<div class="modal-footer">
		        	<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
		        	<button type="submit" class="btn btn-primary">Export to Galaxy</button>
		      	</div>
		    </div>
		</div>
	</div>
</form>
</#if>
<script>
	<#-- load css dependencies -->
	if (!$('link[href="/css/jquery.molgenis.table.css"]').length)
		$('head').append('<link rel="stylesheet" href="/css/jquery.molgenis.table.css" type="text/css" />');
	<#-- load js dependencies -->
	$.when(
		$.ajax("/js/jquery.bootstrap.pager.js", {'cache': true}),
		$.ajax("/js/jquery.molgenis.table.js", {'cache': true}),
		$.ajax("/js/dalliance-compiled.js", {'cache': true}),
		$.ajax("/js/dataexplorer-data.js", {'cache': true}))
		.done(function() {
			<#-- create genome browser -->
            if(molgenis.dataexplorer.data.doShowGenomeBrowser() == true)
            {
                molgenis.dataexplorer.data.createGenomeBrowser(
                {
                    ${initLocation},
                    coordSystem: ${coordSystem},
                    chains: ${chains},
                    sources: ${sources},
                    browserLinks: ${browserLinks}
                }, [<#list genomeEntities?keys as entityName>{'name': '${entityName}', 'label': '${genomeEntities[entityName]}'}<#if entityName_has_next>,</#if></#list>]);
            }
            else
            {
                $('#genomebrowser').css('display', 'none');
            }

			<#-- create data table -->
			var tableEditable = ${tableEditable?string('true', 'false')};
			if (tableEditable) {
				tableEditable = molgenis.hasWritePermission(molgenis.dataexplorer.getSelectedEntityMeta().name);
			}
			molgenis.dataexplorer.data.createDataTable(tableEditable);    	
		})
		.fail(function() {
			molgenis.createAlert([{'message': 'An error occured. Please contact the administrator.'}], 'error');
		});
</script>