<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#include "sorta-match-view-result.ftl">
<#include "sorta-match-new-task.ftl">
<#include "sorta-match-list-tasks.ftl">
<#assign css=["jasny-bootstrap.min.css", "ui.fancytree.min.css", "ontology-service.css", "biobank-connect.css"]>
<#assign js=["jasny-bootstrap.min.js", "jquery.fancytree.min.js", "common-component.js", "ontology-service-result.js", "jquery.bootstrap.pager.js", "simple_statistics.js"]>
<@header css js/>
<form id="ontology-match" class="form-horizontal" enctype="multipart/form-data">
    <div class="row">
        <div class="col-md-12">
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
        <#if existingTasks??>
            <@listTasks />
        <#else>
            <div class="row">
                <div class="col-md-offset-2 col-md-2">
                    <button id="back-button" type="button" class="btn btn-primary">Restart</button>
                </div>
            </div>
            <script>
                $(document).ready(function () {
                    $('#back-button').click(function () {
                        $('#ontology-match').attr({
                            'action': molgenis.getContextUrl(),
                            'method': 'GET'
                        }).submit();
                    });
                });
            </script>
            <#if ontologies??>
                <@ontologyMatchNewTask />
            <#else>
                <@ontologyMatchResult />
            </#if>
        </#if>
        </div>
    </div>
    <script type="text/javascript">
        $(document).ready(function () {
        <#if message??>
            var molgenis = window.top.molgenis;
            molgenis.createAlert([{
                'message': '${message?js_string}'
            }], 'error');
        </#if>
        });
    </script>
</form>
<@footer/>	