<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["thememanager.js"]>

<@header css js/>

<div class="row">
    <div class="col-md-12">
        <legend>Select a bootstrap theme</legend>
        <form class="form-inline" role="form">
            <div class="form-group">
                <div class="col-md-4">
                    <select class="form-control" id="bootstrap-theme-select">
                    <#list availableStyles as style>
                        <option value="${style.location}"
                                <#if selectedStyle?? && style.name == selectedStyle>selected</#if>>${style.name}</option>
                    </#list>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-md-4">
                    <button id="save-selected-bootstrap-theme" type="btn" class="btn btn-primary">Save current theme
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <br></br>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
        <legend>Component overview</legend>
        <p>TODO: Overview of all the different bootstrap components to showcase the new style</p>
    </div>
</div>

<@footer/>
