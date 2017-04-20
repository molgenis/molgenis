<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=['jquery.validate.min.js', 'scripts.js']>

<@header css js/>
<div class="container-fluid">
    <div class="row">
        <div class="col-md-12">
            <legend>
                Scripts
            <@hasPermission entityTypeId='sys_scr_Script' permission="WRITE">
                <a id="create-script-btn" href="#" style="margin:30px 10px"><img src="/img/new.png"></a>
            </@hasPermission>
            </legend>

            <table class="table table-condensed table-bordered">
                <thead>
                <tr>
                <@hasPermission entityTypeId='sys_scr_Script' permission="WRITE">
                    <th class="edit-icon-holder"></th>
                </@hasPermission>
                <@hasPermission entityTypeId='sys_scr_Script' permission="WRITE">
                    <th class="edit-icon-holder"></th>
                </@hasPermission>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Result file extension</th>
                    <th>Parameters</th>
                    <th>Execute</th>
                </tr>
                </thead>

                <tbody>
                <#if scripts?has_content>
                    <#list scripts as script>
                    <tr>
                        <@hasPermission entityTypeId='sys_scr_Script' permission="WRITE">
                            <td>
                                <a href="#" class="edit-script-btn"><img src="/img/editview.gif"></a>
                            </td>
                        </@hasPermission>
                        <@hasPermission entityTypeId='sys_scr_Script' permission="WRITE">
                            <td>
                                <a href="#" class="delete-script-btn"><img src="/img/delete.png"></a>
                            </td>
                        </@hasPermission>
                        <td class="name">${script.name!?html}</td>
                        <td>${script.type.name!?html}</td>
                        <td>${script.resultFileExtension!?html}</td>
                        <td class="parameters">
                            <#if script.parameters?has_content>
                                <#list script.parameters as parameter>
                                ${parameter.name?html}<#if parameter_has_next>,</#if>
                                </#list>
                            </#if>
                        </td>
                        <td>
                            <a href="#" data-hasAttributes="${script.parameters?has_content?string("true","false")}"
                               class="execute">
                                <span class="glyphicon glyphicon-refresh"></span>
                            </a>
                        </td>
                    </tr>
                    </#list>
                </#if>
                </tbody>
            </table>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <legend>
                Parameters
            <@hasPermission entityTypeId='sys_scr_ScriptParameter' permission="WRITE">
                <a id="create-scriptparameter-btn" href="#" style="margin:30px 10px">
                    <img src="/img/new.png">
                </a>
            </@hasPermission>
            </legend>

            <table class="table table-condensed table-bordered" style="width: 25%">
                <thead>
                <tr>
                    <th>Name</th>
                <@hasPermission entityTypeId='sys_scr_ScriptParameter' permission="WRITE">
                    <th class="edit-icon-holder"></th>
                </@hasPermission>
                <tr>
                </thead>

                <tbody>
                <#if parameters?has_content>
                    <#list parameters as parameter>
                    <tr>
                        <td class="name">${parameter.name!?html}</td>
                        <@hasPermission entityTypeId='sys_scr_ScriptParameter' permission="WRITE">
                            <td>
                                <a href="#" class="delete-script-parameter-btn"><img src="/img/delete.png"></a>
                            </td>
                        </@hasPermission>
                    </tr>
                    </#list>
                </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="modal" id="parametersModal" tabindex="-1" role="dialog" aria-labelledby="parametersModal-label"
     aria-hidden="true">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span
                        class="sr-only">Close</span></button>
                <h4 class="modal-title" id="parametersModal-label">Parameters</h4>
            </div>
            <div class="modal-body">
                <form id="parametersForm" class="form-horizontal"></form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" id="runWithParametersButton" class="btn btn-primary">Run</button>
            </div>
        </div>
    </div>
</div>

<script id="parameters-template" type="text/x-handlebars-template">
    {{#each parameters}}
    <div class="form-group">
        <label class="col-md-3 control-label">{{name}}</label>
        <div class="col-md-9">
            <input type="text" name="{{name}}" value="" class="form-control" required>
        </div>
    </div>
    {{/each}}
</script>
<@footer/>