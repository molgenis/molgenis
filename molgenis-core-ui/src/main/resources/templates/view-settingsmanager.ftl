<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["settingsmanager.js"]>

<@header css js/>
<div class="row">
    <div class="col-md-offset-2 col-md-8">
        <div class="row">
            <div class="col-md-12">
                <div class="well">
                    <div class="row">
                        <div class="col-md-offset-2 col-md-8">
                            <div id="settings-select-container"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div id="settings-container"></div>
            </div>
        </div>
    </div>
</div>
<@footer/>