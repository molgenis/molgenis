<#macro ontologyMatchResult>
<div class="row">
    <div class="col-md-12">
        <div class="row" style="margin-bottom:15px;">
            <div class="col-md-offset-3 col-md-6">
                <div class="row">
                    <div class="col-md-6">
                        <p style="padding-top:5px;margin-left:-15px;">Current threshold : ${threshold?html}%</p>
                    </div>
                    <div class="col-md-6">
                        <input name="threshold" type="text" class="form-control"
                               style="width:50px;float:right;margin-right:-15px;"/>
                        <button id="update-threshold-button" class="btn btn-default float-right" type="button">Update
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
            $(document).ready(function () {

                var request = {
                    'sortaJobExecutionId': '${sortaJobExecutionId?js_string}',
                    'ontologyIri': '${ontologyIri?js_string}',
                'matched' : <#if isMatched?? && isMatched>true<#else>false</#if>
                };

                var ontologyService = new molgenis.OntologyService($('#match-result-container'), request);
                ontologyService.renderPage();

                $('#update-threshold-button').click(function () {
                    $(this).parents('form:eq(0)').attr({
                        'action': molgenis.getContextUrl() + '/threshold/${sortaJobExecutionId?html}',
                        'method': 'POST'
                    }).submit();
                });
            });
        </script>
    </div>
</div>
</#macro>