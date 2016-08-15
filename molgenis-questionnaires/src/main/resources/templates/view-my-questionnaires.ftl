<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=[]>

<@header css js/>

<div class="row">
    <div class="col-md-6">
        <h1>${i18n.questionnaires_title?html}</h1>
        <p>${i18n.questionnaires_description?html}</p>
    </div>
</div>

<#if questionnaires?size == 0>
<h3>${i18n.questionnaires_no_questionnaires_found_message?html}</h3>
<#else>
<div class="row">
    <div class="col-md-6">
        <table class="table table-bordered">
            <thead>
            <tr>
                <th>${i18n.questionnaires_table_questionnaire_header?html}</th>
                <th>${i18n.questionnaires_table_status_header?html}</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
                <#list questionnaires as questionnaire>
                <tr>
                    <td>${questionnaire.label!?html}</td>
                    <#if questionnaire.status == 'NOT_STARTED'>
                        <td>${i18n.questionnaires_table_status_not_started?html}</td>
                        <td>
                            <a class="btn btn-primary"
                               href="${context_url}/${questionnaire.name?url('UTF-8')}">${i18n.questionnaires_table_start_questionnaire_button?html}</a>
                        </td>
                    <#elseif questionnaire.status == 'OPEN'>
                        <td>${i18n.questionnaires_table_status_open?html}</td>
                        <td>
                            <a class="btn btn-primary"
                               href="${context_url}/${questionnaire.name?url('UTF-8')}">${i18n.questionnaires_table_continue_questionnaire_button?html}</a>
                        </td>
                    <#elseif questionnaire.status == 'SUBMITTED'>
                        <td>${i18n.questionnaires_table_status_submitted?html}</td>
                        <td>
                            <a class="btn btn-primary"
                               href="${context_url}/${questionnaire.name?url('UTF-8')}">${i18n.questionnaires_table_view_questionnaire_button?html}</a>
                        </td>
                    </#if>
                </tr>
                </#list>
            </tbody>
        </table>
    </div>
</div>
</#if>