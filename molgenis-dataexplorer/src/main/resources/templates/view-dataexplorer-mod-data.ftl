<#include "resource-macros.ftl">
<div id="dataexplorer-grid-data">
    <div class="row">
        <div class="col-md-12">
            <div class="panel" id="genomebrowser">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        <a data-toggle="collapse" data-target="#genomebrowser-collapse" href="#genomebrowser-collapse">Genome
                            Browser</a>
                    </h4>
                </div>
                <div id="genomebrowser-collapse" class="panel-collapse collapse in">
                    <div class="panel-body">
                    <#-- dalliance default id to print browser -->
                        <div id="svgHolder"></div>
                        <div class="pull-right"><a id="genomebrowser-filter-button" class="btn btn-default btn-sm"><img
                                src="/img/filter-bw.png"> Apply filter</a></div>
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
                    <a id="download-modal-button" class="btn btn-default" data-toggle="modal"
                       data-target="#downloadModal">Download</a>
                <#if showDirectoryButton>
                    <a id="directory-export-button"
                       class="btn btn-default">${i18n.dataexplorer_directory_export_button?html}</a>
                </#if>
                </div>
            </div>
        </div>
    </div>
</div>

<#-- CSV download modal -->
<div class="modal" id="downloadModal" tabindex="-1" role="dialog" aria-labelledby="download-modal-label"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">

            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span
                        class="sr-only">Close</span></button>
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
                            <input type="radio" name="colNames" value="ATTRIBUTE_NAMES"> Attribute Names
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
                            <input type="radio" name="downloadTypes" value="DOWNLOAD_TYPE_XLSX"> XLSX
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

</div>
</div>
<!--[if IE 9]>
<#-- used to disable the genomebrowser in IE9 -->
    <script>top.molgenis.ie9 = true;</script>
<![endif]-->
<script>
    <#-- used to disable the genomebrowser in IE10 (which no longer supports conditional comments) -->
    if (Function('/*@cc_on return document.documentMode===10@*/')()) {
        top.molgenis.ie10 = true
    }
    <#-- load js dependencies -->
    $.when(
            $.ajax('<@resource_href "/js/dataexplorer-data.js"/>', {'cache': true}),
            $.ajax('<@resource_href "/js/dalliance-all.min.js"/>', {'cache': true}),
            $.ajax('<@resource_href "/js/dataexplorer-directory.js"/>', {'cache': true}))
            .done(function () {
            <#-- do *not* js escape values below -->
                molgenis.dataexplorer.data.setGenomeBrowserSettings({
                ${plugin_settings.gb_init_location},
                        coordSystem
            : ${plugin_settings.gb_init_coord_system},
                sources: ${plugin_settings.gb_init_sources},
                browserLinks: ${plugin_settings.gb_init_browser_links}
                        }
            )

                molgenis.dataexplorer.data.setGenomeBrowserTracks([<#list genomeTracks as genomeTrack>${genomeTrack},</#list>])
            <#if pos_attr?? && chrom_attr??>
                molgenis.dataexplorer.setGenomeAttributes('${pos_attr}', '${chrom_attr}')
                molgenis.dataexplorer.data.setGenomeBrowserAttributes('${pos_attr}', '${chrom_attr}')
            </#if>
                if (molgenis.dataexplorer.data.doShowGenomeBrowser() === true) {
                    molgenis.dataexplorer.data.createGenomeBrowser({showHighlight: ${plugin_settings.gb_init_highlight_region?c}})
                }
                else {
                    $('#genomebrowser').css('display', 'none')
                }

                molgenis.dataexplorer.data.createDataTable()
            })
            .fail(function () {
                molgenis.createAlert([{'message': 'An error occured. Please contact the administrator.'}], 'error')
            })
</script>