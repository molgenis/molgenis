<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['questionnaire.css']>
<#assign js=['questionnaire.js']>

<@header css js/>

<a href="/menu/main/questionnaires" class="btn btn-default btn-md"><span
        class="glyphicon glyphicon-chevron-left"></span> ${i18n.questionnaire_back_button?html}</a>

<div class="row">
    <div class="col-md-8">
        <h1>${questionnaire.label!}</h1>
        <p>${questionnaire.description!}</p>
        <legend></legend>
    </div>
</div>

<div class="row">
    <div id="form-holder" data-name="${questionnaire.name!?url('UTF-8')}" data-id="${questionnaire.id!}"
         class="col-md-10"></div>
</div>

<@footer />