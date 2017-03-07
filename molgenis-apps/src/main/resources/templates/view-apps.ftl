<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["apps.css"]>
<#assign js=["apps.js"]>

<@header css js/>
<@hasPermission entityName='${appEntityTypeFullyQualifiedName}' permission="WRITE">
<div class="row header">
    <div class="col-md-12">
        <button class="btn btn-default" id="create-app-btn">Create new App</button>
    </div>
</div>
</@hasPermission>
<div class="row">
<#list apps as app>
    <#if app?index gt 0 && app?index % 4 == 0>
    </div>
    <div class="row">
    </#if>
    <div class="col-md-3">
        <div class="thumbnail">
            <div class="well">
                <div class="<#if app.active>active<#else>inactive</#if>">
                    <#if app.active>
                    <a target="_blank" href="/menu/main/apps/${app.name}">
                    </#if>
                    <img src="${app.iconHref!"/img/logo_molgenis.gif"}" alt="App icon" class="app-logo">
                    <#if app.active>
                    </a>
                    </#if>
                </div>
                <div class="caption">
                    <h3>${app.name}</h3>
                    <p>${app.description!}</p>
                </div>
                <@hasPermission entityName='${appEntityTypeFullyQualifiedName}' permission="WRITE">
                    <button class="btn btn-warning edit-app-btn" data-appname="${app.name}">Edit app details</button>
            <#if !app.active>
                <button class="btn btn-success activate-app-btn" data-appname="${app.name}">Activate app</button>
            <#else>
                <button class="btn btn-default deactivate-app-btn" data-appname="${app.name}">Deactivate app</button>
            </#if>
                </@hasPermission>
            </div>
        </div>
    </div>
</#list>
</div>
<div id="create-app-form"></div>
<div id="edit-app-form"></div>

<@footer/>