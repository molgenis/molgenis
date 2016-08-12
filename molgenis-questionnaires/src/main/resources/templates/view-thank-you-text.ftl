<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header js=['tinymce/tinymce.min.js', 'thankyoutext.js'] />

<h1>Edit thank you text</h1>

<div class="row">
    <div class="col-md-12">
        <p>Select questionnaire:</p>
        <select name="questionnaire" id="questionnairesDropdown">
        <#list questionnaires as questionnaire>
            <option <#if selectedQuestionnaire?? && selectedQuestionnaire == questionnaire['fullName']>selected</#if>
                    value="${context_url}?questionnaireName=${questionnaire['fullName']?url('UTF-8')}">${questionnaire['label']!?html}</option>
        </#list>
        </select>
    </div>
</div>

<hr></hr>

<#if edit??>
<div class="row">
    <div class="col-md-12">
        <form id="contentForm" method="post" role="form">
            <div class="form-group">
                <div class="col-md-10">
                    <textarea id="content" name="content" form="contentForm" rows="15">${content!}</textarea>
                </div>
            </div>
            <div class="col-md-10">
                <hr></hr>
                <div class="pull-right">
                    <a id="cancelBtn" href="${context_url}?questionnaireName=${selectedQuestionnaire?url('UTF-8')}"
                       class="btn btn-primary">Cancel</a>
                    <button id="submitBtn" type="submit" class="btn btn-success"><span
                            class="glyphicon glyphicon-play"></span> Save
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>
<#else>
<div class="row">
    <div class="col-md-12">
    <#-- Do *not* HTML escape content -->
			${content!}
    </div>
</div>
<div class="row">
    <div class="col-md-12">
        <hr></hr>
        <a href="#" id="editButton" class="btn btn-primary pull-left">Edit page</a>
    </div>
</div>
</#if>

<@footer/>