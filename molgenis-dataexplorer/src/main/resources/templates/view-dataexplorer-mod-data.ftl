<#include "resource-macros.ftl">
<div id="dataexplorer-grid-data">
    <div class="row">
        <div class="col-md-12">
            <div class="panel" id="genomebrowser">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a data-toggle="collapse" data-target="#genomebrowser-collapse" href="#genomebrowser-collapse">Genome Browser</a>
                    </h4>
                </div>
                <div id="genomebrowser-collapse" class="panel-collapse collapse in">
                    <div class="panel-body">
                        <#-- dalliance default id to print browser -->
                        <div id="svgHolder"></div>
                        <div class="pull-right"><a id="genomebrowser-filter-button" class="btn btn-default btn-sm"><img src="/img/filter-bw.png"> Apply filter</a></div>
                    </div>
                </div>
            </div>
        </div>
	</div>
	<div class="row">
        <div class="col-md-12">
            <div class="data-table-container" id="data-table-container"></div>
        </div>
    </div>
    <div class="row">
    	<div class="col-md-12">
			<div class="data-table-pager-container">
				<form name="action-form" class="form-horizontal" action="${context_url?html}/action" method="POST">
					<div class="btn-group pull-right">
					  <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
					    Send to ... <span class="caret"></span>
					  </button>
					  <ul class="dropdown-menu" role="menu">
					  		<li><a href="#" class="download-csv-btn" data-toggle="modal" data-target="#downloadModal">CSV Download</a></li>
					  	<#if galaxyEnabled?? && galaxyEnabled == true>
					  		<li><a href="#" class="export-galaxy-btn" data-toggle="modal" data-target="#galaxy-export-modal">Galaxy</a></li>
					  	</#if>
						<#if actionHandlers?keys?size != 0>
							<li role="presentation" class="divider"></li>
					  		<li role="presentation" class="dropdown-header">Workflows</li>
					  	</#if>
					  	<#list actionHandlers?keys as key>
							<li><a href="#" class="action-btn" data-id="${key?html}">${actionHandlers[key].actionLabel?html}</a></li>
						</#list>
					  </ul>
					</div>
				</form>
			</div>
		</div>
	</div>
</div>

<#-- CSV download modal -->
<div class="modal" id="downloadModal" tabindex="-1" role="dialog" aria-labelledby="download-modal-label" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">

	      	<div class="modal-header">
	        	<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
	        	<h4 class="modal-title" id="download-modal-label">Download as csv</h4>
	     	</div>

	      	<div class="modal-body">
	      		<div class="form-group form-horizontal">
		      		<div class="row">
						<div class="control-group">
							<label class="col-md-3 control-label">As column names I want:</label>
							<div class="controls col-md-9">
								<label class="radio">
		  							<input type="radio" name="colNames" value="ATTRIBUTE_LABELS" checked> Attribute Labels
			    				</label>
								<label class="radio">
		  							<input type="radio" name="colNames" value="ATTRIBUTE_NAMES">Attribute Names
								</label>
		  					</div>
						</div>
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

<#-- Entity report modal placeholder -->
<div id="entityReport"></div>

<#if galaxyEnabled?? && galaxyEnabled == true>
<#-- Galaxy export modal -->
<form name="galaxy-export-form" class="form-horizontal" action="${context_url?html}/galaxy/export" method="POST">
	<div class="modal" id="galaxy-export-modal" tabindex="-1" role="dialog" aria-labelledby="galaxy-export-modal-label" aria-hidden="true">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
		      	<div class="modal-header">
    		      	<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                    <h4 class="modal-title" id="galaxy-export-modal-label">Export data set to Galaxy</h4>
		     	</div>
		      	<div class="modal-body">
		      		<div class="form-group">
			      		<label class="col-md-3 control-label" for="galaxy-export-url">Galaxy server URL *</label>
					    <div class="col-md-5">
					    	<input type="text" class="form-control" id="galaxy-export-url" name="galaxyUrl" <#if galaxyUrl??>value="${galaxyUrl?html}" </#if>required><span class="help-block">e.g. https://usegalaxy.org/</span>
					    </div>
		      		</div>
		      		<div class="form-group">
		      			<label class="col-md-3 control-label" for="galaxy-export-api-key">Galaxy API key *</label>
					    <div class="col-md-5">
					    	<input type="password" class="form-control" id="galaxy-export-api-key" name="galaxyApiKey" <#if galaxyApiKey??>value="${galaxyApiKey?html}" </#if>required><span class="help-block">See 'Select API Keys' in menu 'User' on e.g. https://usegalaxy.org/</span>
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
    </div>
</div>
<script>
    molgenis.dataexplorer.setGenomeAttributes('${genomebrowser_start_list?js_string}', '${genomebrowser_chrom_list?js_string}', '${genomebrowser_id_list?js_string}', '${genomebrowser_patient_list?js_string}');
    <#-- load css dependencies -->
	if (!$('link[href="<@resource_href '/css/jquery.molgenis.table.css'/>"]').length)
		$('head').append('<link rel="stylesheet" href="<@resource_href "/css/jquery.molgenis.table.css"/>" type="text/css" />');
	<#-- load js dependencies -->
	$.when(
		$.ajax("<@resource_href "/js/jquery.bootstrap.pager.js"/>", {'cache': true}),
		$.ajax("<@resource_href "/js/jquery.molgenis.table.js"/>", {'cache': true}),
		$.ajax("<@resource_href "/js/dalliance-compiled.js"/>", {'cache': true}),
		$.ajax("<@resource_href "/js/dataexplorer-data.js"/>", {'cache': true}))
		.done(function() {
    			molgenis.dataexplorer.data.setGenomeBrowserAttributes('${genomebrowser_start_list?js_string}', '${genomebrowser_chrom_list?js_string}', '${genomebrowser_id_list?js_string}', '${genomebrowser_patient_list?js_string}');
                <#-- do *not* js escape values below -->    
                molgenis.dataexplorer.data.setGenomeBrowserSettings({
			    ${initLocation},
				coordSystem: ${coordSystem},
				sources: ${sources},
				browserLinks: ${browserLinks}
			});
			molgenis.dataexplorer.data.setGenomeBrowserEntities([<#list genomeEntities?keys as entityName>{'name': '${entityName?js_string}', 'label': '${genomeEntities[entityName]?js_string}'}<#if entityName_has_next>,</#if></#list>])
			
			if(molgenis.dataexplorer.data.doShowGenomeBrowser() == true)
		        {
		            molgenis.dataexplorer.data.createGenomeBrowser({showHighlight: ${showHighlight?js_string}});
		        }
		    else
		        {
		            $('#genomebrowser').css('display', 'none');
		        }

			<#-- create data table -->
			var rowClickable = ${rowClickable?string('true', 'false')};
			var tableEditable = ${tableEditable?string('true', 'false')};
			if (tableEditable) {
				tableEditable = molgenis.hasWritePermission(molgenis.dataexplorer.getSelectedEntityMeta().name);
			}
			molgenis.dataexplorer.data.createDataTable(tableEditable, rowClickable);
		})
		.fail(function() {
			molgenis.createAlert([{'message': 'An error occured. Please contact the administrator.'}], 'error');
		});
</script>