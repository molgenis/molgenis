<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["thememanager.js"]>
<#assign version = 2>

<@header css js version/>
<div class="row" id="thememanager">
    <div class="col-md-12" style="margin-bottom: 1rem">
        <legend>Select a bootstrap theme</legend>
        <form class="form-inline" role="form">
            <div class="form-group">
                <div class="col-md-4">
                    <select class="form-control"
                            id="bootstrap-theme-select"
                            v-model = "selectedTheme">
                        <option v-for="theme in themes" :value="theme.id">{{theme.name || theme.id}}</option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-md-4">
                    <button id="save-selected-bootstrap-theme"
                            type="button"
                            @click="save"
                            class="btn btn-primary">Save
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>
<script src="/js/thememanager.js"></script>
<@footer/>
