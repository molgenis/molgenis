<#include "resource-macros.ftl">
<#-- modal for single entity data -->
<div class="modal large" id="entityReportModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xxl">
        <div class="modal-content">
        <#if showStandaloneReportUrl>
            <form class="form-inline" style="text-align: center">
                <hr>
                <div class="form-group">
                    <div class="col-md-12">
                        <div class="col-md-6">
                        <label for="standalone-report-url">Share this entity report</label>
                        <input type="text" class="form-control" id="standalone-report-url"
                               readonly="readonly">
                        </div>
                    </div>
                </div>
            <#--    <button title="Copy to clipboard" class="btn btn-default"
                        id="copy-standalone-report-url-btn"><span
                        class="glyphicon glyphicon-duplicate" aria-hidden="true"></span></button> -->
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

                // FIXME get copy functionality working
//                document.addEventListener('copy', function (e) {
//                    e.preventDefault()
//                    e.clipboardData.setData('text/plain', 'standaloneReportURL')
//                })
//
//                document.getElementById('copy-standalone-report-url-btn').addEventListener('click', function (e) {
//                    e.preventDefault()
//                    document.execCommand('copy')
//                })
            })
    )
</script>