<#import "spring.ftl" as spring />
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=['bwizard.min.css', 'molgenis-wizard.css'] + stylesheets>
<#assign js=['molgenis-wizard.js', 'bootstrap.file-input.js'] + javascripts>

<@header css js/>
<div class="row">
    <div class="col-md-12">
        <div id="wizard">
            <ol class="bwizard-steps clearfix" role="tablist">
            <#list wizard.pages as wizardPage>
                <#if wizardPage == wizard.currentPage>
                    <li role="tab" class="active">
                        <span class="badge inverse">${(wizardPage_index + 1)?html}</span> ${wizardPage.title?html}
                    </li>
                <#else>
                    <li role="tab">
                        <span class="badge">${(wizardPage_index + 1)?html}</span> ${wizardPage.title?html}
                    </li>
                </#if>
            </#list>
            </ol>

        <@spring.bind "wizard" />
        <#if spring.status.error>
            <script>
                molgenis.createAlert([{'message': '<#list spring.status.errorMessages as error>${error?js_string}</#list>'}], 'error');
            </script>
        </#if>

        <#list wizard.pages as wizardPage>
            <#if wizardPage == wizard.currentPage>
                <div class="wizard-page well">
                    <#include wizardPage.freemarkerTemplateName />
                </div>
            </#if>
        </#list>

            <ul class="pager bwizard-buttons">
                <li role="button" class="previous <#if !wizard.previousButton.enabled> disabled</#if>">
                    <a <#if wizard.previousButton.id?has_content>id="${wizard.previousButton.id?html}"</#if>
                       href="${context_url?html}${wizard.previousButton.targetUri?html}">&larr; ${wizard.previousButton.title?html}</a>
                </li>
                <li class="next pull-right<#if !wizard.nextButton.enabled> disabled</#if>" role="button">
                    <a <#if wizard.nextButton.id?has_content>id="${wizard.nextButton.id?html}"</#if>
                       href="${context_url?html}${wizard.nextButton.targetUri?html}">${wizard.nextButton.title?html} &rarr;</a>
                </li>
                <li class="restart pull-right" role="button">
                    <a href="${context_url?html}/restart">Restart</a>
                </li>
            </ul>
        </div>
    </div>
</div>
<@footer/>