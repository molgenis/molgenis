<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["logmanager.js"]>

<@header css js/>
<div class="row">
    <div class="col-md-offset-2 col-md-8">
        <p>Runtime create loggers and change the level of existing loggers, changes are not persisted after server
            restart.</p>
        <div class="row">
            <div class="col-md-7">
                <table class="table table-striped">
                    <thead>
                    <th>Name</th>
                    <th>Level</th>
                    </thead>
                    <tbody>
                    <#list loggers as logger>
                    <tr data-logger="${logger.name?html}">
                        <td>${logger.name?html}</td>
                        <td>
                            <select class="log-level-select"<#if !hasWritePermission> disabled</#if>>
                                <#list levels as level>
                                    <option<#if logger.level == level> selected</#if>>${level?html}</option>
                                </#list>
                            </select>
                        </td>
                    </tr>
                    </#list>
                    </tbody>
                </table>
                <button type="button" class="btn btn-default" id="reset-loggers-btn"<#if !hasWritePermission>
                        disabled</#if>>Reset Loggers
                </button>
            </div>
            <div class="col-md-5">
                <legend>Create Logger</legend>
                <form name="create-logger-form" class="form-horizontal" role="form">
                    <div class="form-group">
                        <div class="col-md-3">
                            <label class="control-label pull-right" for="logger-name">Name *</label>
                        </div>
                        <div class="col-md-9">
                            <input type="text" class="form-control" name="logger-name" id="logger-name" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-9 col-md-offset-3">
                            <button type="submit" class="btn btn-default"<#if !hasWritePermission> disabled</#if>>
                                Create
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
<@footer/>