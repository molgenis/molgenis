<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["permissionmanager.css"]>
<#assign js=["permissionmanager.js"]>
<@header css js/>
<div class="row">
    <div class="col-md-10 col-md-offset-1">
        <div class="row">
            <div class="col-md-3">
                <ul class="nav nav-pills nav-stacked" role="tablist">
                    <li class="active"><a href="#plugin-permission-manager" role="tab" data-toggle="tab">Plugin
                        Permissions</a></li>
                    <li><a href="#package-permission-manager" role="tab" data-toggle="tab">Package Permissions</a></li>
                    <li><a href="#entity-class-permission-manager" role="tab" data-toggle="tab">Entity Class
                        Permissions</a></li>
                    <li><a href="#entity-permission-manager" role="tab" data-toggle="tab">Row-Level Security</a></li>
                </ul>
            </div>
            <div class="col-md-9">
                <div class="tab-content">
                    <div class="tab-pane active" id="plugin-permission-manager">
                    <#include "/view-permissionmanager-plugin.ftl">
                    </div>
                    <div class="tab-pane" id="package-permission-manager">
                    <#include "/view-permissionmanager-package.ftl">
                    </div>
                    <div class="tab-pane" id="entity-class-permission-manager">
                    <#include "/view-permissionmanager-entity-class.ftl">
                    </div>
                    <div class="tab-pane" id="entity-permission-manager">
                    <#include "/view-permissionmanager-entity.ftl">
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<@footer/>