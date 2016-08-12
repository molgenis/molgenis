<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#include "sorta-match-new-task.ftl">
<#assign css=["jasny-bootstrap.min.css", "ontology-service.css", "biobank-connect.css"]>
<#assign js=["jasny-bootstrap.min.js", "sorta-result-anonymous.js"]>
<@header css js/>
<form id="ontology-match" class="form-horizontal" enctype="multipart/form-data">
    <br>
    <div class="row">
        <div class="col-md-offset-3 col-md-6">
            <legend>
                <center><strong>SORTA</strong> - <strong>S</strong>ystem for <strong>O</strong>ntology-based
                    <strong>R</strong>e-coding and <strong>T</strong>echnical <strong>A</strong>nnotation
                </center>
            </legend>
        </div>
    </div>
<#if showResult?? && showResult>
    <script type="text/javascript">
        $(document).ready(function () {
            var sorta = new window.top.molgenis.SortaAnonymous($('#ontology-match'));
            sorta.renderPage();
        });
    </script>
<#else>
    <@ontologyMatchNewTask />
    <script type="text/javascript">
        $(document).ready(function () {
            <#if message??>
                window.top.molgenis.createAlert([{'message': '${message?js_string}'}], 'error');
            </#if>
            $('#ontology-match').children('div.row:eq(1)').remove();
        });
    </script>
</#if>
</form>
<@footer/>