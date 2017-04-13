<#include "resource-macros.ftl">
<#-- modal for single entity data -->
<div class="modal large" id="entityReportModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xxl">
        <div class="modal-content">
        <#if showStandaloneReportUrl>
            <form class="form-inline">
                <hr>
                <div class="form-group pull-right" style="margin-left: 2em;margin-right:1em;">
                    <div class="row">
                        <div class="col-md-12">
                            <small><label for="standalone-report-url">Share this entity report</label></small>
                            <div class="row">
                                <div class="col-md-12">
                                    <input type="text" class="form-control input-sm" id="standalone-report-url"
                                           readonly="readonly">
                                    <button title="Copy to clipboard" class="btn btn-default btn-sm"
                                            id="copy-standalone-report-url-btn"><span
                                            class="glyphicon glyphicon-duplicate" aria-hidden="true"></span></button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <hr>
            </form>
        </#if>
        <#include viewName+".ftl">
        </div>
    </div>
</div>

<script>
    $.when(
            $.ajax('<@resource_href "/js/dataexplorer-data.js"/>', {'cache': true}).done(function () {
                var entityId = '${entityId}'
                var entityName = '${entityName}'
                var standaloneReportURL = window.location.origin + molgenis.getContextUrl() + '/details/' + entityName + '/' + entityId

                $('#standalone-report-url').val(standaloneReportURL)
                //Add copy functionality
                document.getElementById('copy-standalone-report-url-btn').addEventListener('click', function (e) {
                    e.preventDefault()
                    var url = document.createElement("input");
                    url.setAttribute("value", $("#standalone-report-url").val());
                    document.body.appendChild(url);
                    url.select();
                    document.execCommand("copy");
                })
            })
    )
</script>