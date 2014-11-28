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
			</div>
		</div>
	</div>
</div>

<#-- Entity report modal placeholder -->
<div id="entityReport"></div>


    </div>
</div>
<script>
    molgenis.dataexplorer.setGenomeAttributes('${genomebrowser_start_list}', '${genomebrowser_chrom_list}', '${genomebrowser_id_list}', '${genomebrowser_patient_list}');
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
    			molgenis.dataexplorer.data.setGenomeBrowserAttributes('${genomebrowser_start_list}', '${genomebrowser_chrom_list}', '${genomebrowser_id_list}', '${genomebrowser_patient_list}');

                molgenis.dataexplorer.data.setGenomeBrowserSettings({
			    ${initLocation},
				coordSystem: ${coordSystem},
				sources: ${sources},
				browserLinks: ${browserLinks}
			});
			molgenis.dataexplorer.data.setGenomeBrowserEntities([<#list genomeEntities?keys as entityName>{'name': '${entityName}', 'label': '${genomeEntities[entityName]}'}<#if entityName_has_next>,</#if></#list>])
			
			if(molgenis.dataexplorer.data.doShowGenomeBrowser() == true)
		        {
		            molgenis.dataexplorer.data.createGenomeBrowser({showHighlight: ${showHighlight}});
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