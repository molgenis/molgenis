<#import "spring.ftl" as spring />
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=['bwizard.min.css', 'molgenis-wizard.css'] + stylesheets>
<#assign js=['molgenis-wizard.js'] + javascripts>

<@header css js/>
	<div class="row-fluid">
		<div id="wizard">
			<ol class="bwizard-steps clearfix" role="tablist">
				<#list wizard.pages as wizardPage>
					<#if wizardPage == wizard.currentPage>
						<li role="tab" class="active">
							<span class="label badge-inverse">${wizardPage_index + 1}</span> ${wizardPage.title}
						</li>
					<#else>
						<li role="tab">
							<span class="label">${wizardPage_index + 1}</span> ${wizardPage.title}
						</li>
					</#if>
				</#list>
			</ol> 
				
			<#if successMessage! != "" >
				<div class="alert alert-block alert-success">${successMessage!}</div>
			</#if>
				
			<@spring.bind "wizard" />	
			<#if spring.status.error>
  				<#list spring.status.errorMessages as error>
                	<div class="alert alert-block alert-error">${error}</div>
            	</#list>
			</#if>
				
			<#list wizard.pages as wizardPage>
				<#if wizardPage == wizard.currentPage>
					<div class="wizard-page well">
						<#include wizardPage.freemarkerTemplateName />
					</div>
				</#if>
			</#list>
				
			<ul class="pager bwizard-buttons">
				<li class="previous<#if !wizard.previousButton.enabled> disabled</#if>" role="button">
					<a href="${context_url}${wizard.previousButton.targetUri}">${wizard.previousButton.title}</a>
				</li>
				<li class="next pull-right<#if !wizard.nextButton.enabled> disabled</#if>" role="button">
					<a href="${context_url}${wizard.nextButton.targetUri}">${wizard.nextButton.title}</a>
				</li>
				<li role="button" class="cancel pull-right">
					<a href="${context_url}/restart">Restart</a>
				</li>
			</ul>
		</div>
	</div>
<@footer/>