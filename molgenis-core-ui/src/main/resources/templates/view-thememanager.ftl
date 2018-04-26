<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["thememanager.js"]>

<@header css js/>

<div class="row">
    <div class="col-md-12" style="margin-bottom: 1rem">
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
            <div class="form-group">
                <div class="col-md-4">
                    <button id="show-add-theme-btn" type="button" class="btn btn-primary">Add theme
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<div id="add-theme-container" class="row" style="display: none; margin: 1rem">
    <div class="col-md-12">
        <div class="panel panel-default">
            <div class="panel-body">
                <form>
                    <div class="form-group">
                        <label for="bootstrap3-file">Bootstrap 3 file<span style="color: red"> *</span></label>
                        <input id="bootstrap3-file" type="file" accept=".css, .min.css">
                    </div>
                    <div class="form-group">
                        <label for="bootstrap4-file">Bootstrap 4 file</label>
                        <input type="file" id="bootstrap4-file" accept=".css, .min.css">
                        <p class="help-block">If you do not include a bootstrap 4 variant the 'default' bootstrap theme
                            will be used.</p>
                    </div>
                    <button id="cancel-add-themes-btn" type="button" class="btn btn-default">Cancel</button>
                    <button id="add-themes-btn" type="button" class="btn btn-success">Add theme</button>
                </form>
            </div>
        </div>
    </div>
</div>

<@footer/>
