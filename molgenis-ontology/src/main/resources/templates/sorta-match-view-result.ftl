<#macro ontologyMatchResult>
<div class="row">
    <div class="col-md-12">
        <div class="row" style="margin-bottom:15px;">
            <div class="col-md-offset-3 col-md-6">
                <div class="row">
                    <div class="col-md-12">
                        <input name="threshold" value="${threshold?html}" type="text" class="form-control"
                               style="width:50px;float:right;margin-right:-15px;"/>
                        <button id="update-threshold-button" class="btn btn-default float-right" type="button">Update Threshold
                        </button>
                    </div>
                    <br>
                </div>
            </div>
        </div>
        <div class="row">
            <div id="match-result-container" class="col-md-12"></div>
        </div>
        <script>
            window.sorta = {
                threshold: ${threshold?html},
                request: {
                    sortaJobExecutionId: '${sortaJobExecutionId?js_string}',
                    ontologyIri: '${ontologyIri?js_string}',
                    matched: <#if isMatched?? && isMatched>true<#else>false</#if>
                }
            }
        </script>
    </div>
</div>
</#macro>