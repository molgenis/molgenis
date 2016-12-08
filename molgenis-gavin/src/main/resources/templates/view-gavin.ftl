<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["gavin.js"]>
<@header css js/>

<div class="row" id="gavin-view">
    <div class="col-md-12">
        <div class="panel panel-primary" id="instant-import">
            <div class="panel-heading">
                <h4 class="panel-title">
                    Gavin annotation
                </h4>
            </div>
        <#--noinspection FtlReferencesInspection-->
        <#if annotatorsWithMissingResources?has_content>
            <div class="panel-body">
                <p>The following annotators have missing resources:</p>
                <ul>
                <#--noinspection FtlReferencesInspection-->
                    <#list annotatorsWithMissingResources as ann>
                        <li>${ann}
                            <span id="${ann}-settings-btn" class="glyphicon glyphicon-cog" aria-hidden="true"
                                  style="cursor: pointer; margin-left: 5px;" data-name="${ann}"></span>
                        </li></#list>
                </ul>
            </div>
            <div id="form"></div>
        <#else>
            <div class="panel-body">
                <div id="instant-import-alert"></div>
                <#if content?has_content>
                    <div class="row">
                        <div class="col-md-12">
                        <#-- Do *not* HTML escape content -->
			                ${content}
                        </div>
                    </div>
                </#if>

                <#if isCurrentUserCanEdit?has_content && isCurrentUserCanEdit>
                    <div class="row">
                        <div class="col-md-12">
                            <hr></hr>
                            <a href="${context_url?html}/edit" class="btn btn-default pull-left">Edit page</a>
                        </div>
                    </div>
                </#if>
                <div id="gavin-form"></div>
            </div>
        </#if>
        </div>
    </div>
</div>

<@footer/>