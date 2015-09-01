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
				<div class="pull-right">
					<a id="download-modal-button" class="btn btn-default" data-toggle="modal" data-target="#downloadModal">Download</a>
				<#if plugin_settings.data_galaxy_export == true>
					<a id="galaxy-export-modal-button" class="btn btn-default" data-toggle="modal" data-target="#galaxy-export-modal">Export to Galaxy</a>
				</#if>
				</div>
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
	      	    <form class="form" role="form">
                    <span id="helpBlock" class="help-block">As column names I want:</span>
                    <div class="radio">
                        <label>
                            <input type="radio" name="colNames" value="ATTRIBUTE_LABELS" checked> Attribute Labels
                        </label>
                    </div>
                    <div class="radio">
                        <label>
                            <input type="radio" name="colNames" value="ATTRIBUTE_NAMES">  Attribute Names
                        </label>   
                    </div>
                    
                    <span id="helpBlock" class="help-block">As entity values I want:</span>
                    <div class="radio">
                        <label>
                            <input type="radio" name="entityValues" value="ENTITY_LABELS" checked> Entity labels
                        </label>
                    </div>
                    <div class="radio">
                        <label>
                            <input type="radio" name="entityValues" value="ENTITY_IDS"> Entity ids
                        </label>   
                    </div>
                    
                    <span id="helpBlock" class="help-block">As download type I want:</span>
                    <div class="radio">
	                    <label>
	                        <input type="radio" name="downloadTypes" value="DOWNLOAD_TYPE_CSV" checked> CSV
	                    </label>
                    </div>
                    <div class="radio">
                        <label>
                            <input type="radio" name="downloadTypes" value="DOWNLOAD_TYPE_XLSX">  XLSX
                        </label>   
                    </div>
	      	    </form>
			</div>

	      	<div class="modal-footer">
	        	<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	        	<button type="button" id="download-button" class="btn btn-primary">Download</button>
	      	</div>

	    </div>
	</div>
</div>

<#-- Entity report modal placeholder -->
<div id="entityReport"></div>

<#if plugin_settings.data_galaxy_export == true>
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
					    	<input type="text" class="form-control" id="galaxy-export-url" name="galaxyUrl" <#if plugin_settings.data_galaxy_url??>value="${plugin_settings.data_galaxy_url?html}" </#if>required><span class="help-block">e.g. https://usegalaxy.org/</span>
					    </div>
		      		</div>
		      		<div class="form-group">
		      			<label class="col-md-3 control-label" for="galaxy-export-api-key">Galaxy API key *</label>
					    <div class="col-md-5">
					    	<input type="password" class="form-control" id="galaxy-export-api-key" name="galaxyApiKey" <#if plugin_settings.data_galaxy_api_key??>value="${plugin_settings.data_galaxy_api_key?html}" </#if>required><span class="help-block">See 'Select API Keys' in menu 'User' on e.g. https://usegalaxy.org/</span>
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
    molgenis.dataexplorer.setGenomeAttributes('${genomicDataSettings.start?js_string}', '${genomicDataSettings.chromosome?js_string}', '${genomicDataSettings.identifier?js_string}', '${genomicDataSettings.patient_id?js_string}');
	<#-- load js dependencies -->
	$.when(
		$.ajax("<@resource_href "/js/dalliance-compiled.min.js"/>", {'cache': true}),
		$.ajax("<@resource_href "/js/dataexplorer-data.js"/>", {'cache': true}))
		.done(function() {
    			molgenis.dataexplorer.data.setGenomeBrowserAttributes('${genomicDataSettings.start?js_string}', '${genomicDataSettings.chromosome?js_string}', '${genomicDataSettings.identifier?js_string}', '${genomicDataSettings.patient_id?js_string}');
                <#-- do *not* js escape values below -->    
                molgenis.dataexplorer.data.setGenomeBrowserSettings({
			    ${plugin_settings.gb_init_location},
				coordSystem: ${plugin_settings.gb_init_coord_system},
				sources: ${plugin_settings.gb_init_sources},
				browserLinks: ${plugin_settings.gb_init_browser_links}
			});
			molgenis.dataexplorer.data.setGenomeBrowserEntities([<#list genomeEntities?keys as entityName>{'name': '${entityName?js_string}', 'label': '${genomeEntities[entityName]?js_string}'}<#if entityName_has_next>,</#if></#list>]);
			if(molgenis.dataexplorer.data.doShowGenomeBrowser() === true)
		        {
		            molgenis.dataexplorer.data.createGenomeBrowser({showHighlight: ${plugin_settings.gb_init_highlight_region?c}});
		        }
		    else
		        {
		            $('#genomebrowser').css('display', 'none');
		        }

			molgenis.dataexplorer.data.createDataTable();
		})
		.fail(function() {
			molgenis.createAlert([{'message': 'An error occured. Please contact the administrator.'}], 'error');
		});
</script>