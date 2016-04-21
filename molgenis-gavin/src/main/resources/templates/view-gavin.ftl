<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["gavin.js"]>
<@header css js/>

<div class="row">
    <div class="col-md-12">
        <div class="panel panel-primary" id="instant-import">
            <div class="panel-heading">
                <h4 class="panel-title">
                    Gavin annotation
                </h4>
            </div>
            <#if annotatorsWithMissingResources?has_content>
                You need to configure the following annotators:
                <#list annotatorsWithMissingResources as ann>${ann}</#list>
            <#else>
                <div class="panel-body">
                    <div id="instant-import-alert"></div>
                    <h4>Upload your VCF file and we'll annotate it for you.</h4>
                    <div id="gavin-form"></div>
                </div>
            </#if>
        </div>
    </div>
</div>

<@footer/>