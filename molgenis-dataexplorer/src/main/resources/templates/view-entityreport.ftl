<#-- modal for single entity data -->
<div class="modal large" id="entityReportModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xxl">
        <div class="modal-content">
            <div class="row">
                <div class="col-md12">
                    <p id="copy-entity-row-url">${baseUrl}</p><button class="btn btn-default" id="copy-entity-row-url-btn"><span class="glyphicon glyphicon-duplicate" aria-hidden="true"></span></button>
                </div>
            </div>
        <#include viewName+".ftl">
        </div>
    </div>
</div>