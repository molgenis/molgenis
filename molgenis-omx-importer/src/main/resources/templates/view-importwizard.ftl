<#if enable_spring_ui>
<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=["bwizard.min.css", "importer.css"]>
<#assign js=["bwizard.min.js"]>
<@header css js/>
	<div class="row-fluid">
				<div id="wizard" style="padding:16px">
				<ol>
					<#list wizard.pages as wizardPage>
						<li>${wizardPage.title}</li>
					</#list>
				</ol> 
				
				<#list wizard.pages as wizardPage>
					<div style="min-height: 180px">
						<p>			
							<#if wizardPage == wizard.currentPage && wizardPage.viewTemplate! != "">
								<#if wizard.errorMessage! != "" >
									<div class="alert alert-block alert-error">${wizard.errorMessage!}</div>
								</#if>	
								<#if wizard.validationMessage! != "" >
									<div class="alert alert-block alert-error">${wizard.validationMessage!}</div>
								</#if>	
								<#if wizard.successMessage! != "" >
									<div class="alert alert-block alert-success">${wizard.successMessage!}</div>
								</#if>
								<#include wizardPage.viewTemplate />
							</#if>
						</p>
					</div>
				</#list>
				
			</div>
		
		
	<script type="text/javascript">
		$("#wizard").bwizard({activeIndex: ${wizard.currentPageIndex}});
	   	$('.pager').css({"width" : "491px"});//Pager bar with previous/next buttons
	   	$(window).load(function() {
			var headerHeight = $("#header").height();
			var viewportHeight = $(window).height();
			var otherHeight = 358;//plugin title + menu + padding/progress bar etc of the wizard + footer
			var preferredImporterHeight = (viewportHeight - headerHeight - otherHeight);
	   		
			//TODO:isn't there a way to select those by wildcard? "step*" 
	   		$("#step1").height(preferredImporterHeight);
 			$("#step1").css({"overflow" : "scroll"});
 			$("#step2").height(preferredImporterHeight);
 			$("#step2").css({"overflow" : "scroll"});
 			$("#step3").height(preferredImporterHeight);
 			$("#step3").css({"overflow" : "scroll"});
	    });
	   //Add Cancel button
	   	$('<li role="button" class="cancel" ><a href="#">Restart</a></li>').css({"margin-left" : "230px"}).insertBefore('.next').click(function(){
	   		performAction(this, '/plugin/importwizard');
	   	});
	 
	   	//Remove bwizard default eventhandlers and add our own eventhandlers	
	   	
	   	$('.next').unbind('click').click(function(){
	   		<#if wizard.lastPage > 
	   			performAction(this, '/plugin/importwizard');
	   		<#else>
	   			performAction(this, '/plugin/importwizard/next');
	   		</#if>
	   	});
	   	
	   	$('.previous').unbind('click').click(function(){
	   		performAction(this, '/plugin/importwizard/previous');
	   	});
	   	
	   	<#if wizard.lastPage > 
	    	$('.next a').html('finish');
	    </#if>
	    
	   	<#if wizard.validationMessage! == "">
	   		$('.next').removeClass('disabled');
	   		
	   		<#if wizard.lastPage > 
	   			$('.cancel').addClass('disabled');
	   			$('.previous').addClass('disabled');
	   		</#if>
	   		
	   	<#else>
	   		$('.next').addClass('disabled');
	   	</#if>
	   	
	   	<#if wizard.firstPage && wizard.errorMessage! == "">
	   		$('.cancel').addClass('disabled');
	   	</#if>
	   	 	
	   	function performAction(btn, action) {
	   		if (!$(btn).hasClass('disabled')) {
	   			<#if wizard.currentPageIndex == 2>
	   				$('#spinner').modal('show');
	   			</#if>
	   			
	   			document.forms.importWizardForm.action = action;
	   			document.forms.importWizardForm.submit();
	   		}
	   	}
	</script>
	</div>
<@footer/>
<#else>
<!DOCTYPE html>
<html>
	<head>
		<title>Dataset deletion plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bwizard.min.css" type="text/css">
		<link rel="stylesheet" href="/css/importer.css" type="text/css">
		
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="/js/bwizard.min.js"></script>
	</head>
	<body>
		<div class="container-fluid">
			
			<div id="wizard" style="padding:16px">
				<ol>
					<#list wizard.pages as wizardPage>
						<li>${wizardPage.title}</li>
					</#list>
				</ol> 
				
				<#list wizard.pages as wizardPage>
					<div style="min-height: 180px">
						<p>			
							<#if wizardPage == wizard.currentPage && wizardPage.viewTemplate! != "">
								<#if wizard.errorMessage! != "" >
									<div class="alert alert-block alert-error">${wizard.errorMessage!}</div>
								</#if>	
								<#if wizard.validationMessage! != "" >
									<div class="alert alert-block alert-error">${wizard.validationMessage!}</div>
								</#if>	
								<#if wizard.successMessage! != "" >
									<div class="alert alert-block alert-success">${wizard.successMessage!}</div>
								</#if>
								<#include wizardPage.viewTemplate />
							</#if>
						</p>
					</div>
				</#list>
				
			</div>
		
		</div>
		
	<script type="text/javascript">
		$("#wizard").bwizard({activeIndex: ${wizard.currentPageIndex}});
	   	$('.pager').css({"width" : "491px"});//Pager bar with previous/next buttons
	   	$(window).load(function() {
			var headerHeight = $("#header").height();
			var viewportHeight = $(window).height();
			var otherHeight = 358;//plugin title + menu + padding/progress bar etc of the wizard + footer
			var preferredImporterHeight = (viewportHeight - headerHeight - otherHeight);
	   		
			//TODO:isn't there a way to select those by wildcard? "step*" 
	   		$("#step1").height(preferredImporterHeight);
 			$("#step1").css({"overflow" : "scroll"});
 			$("#step2").height(preferredImporterHeight);
 			$("#step2").css({"overflow" : "scroll"});
 			$("#step3").height(preferredImporterHeight);
 			$("#step3").css({"overflow" : "scroll"});
	    });
	   //Add Cancel button
	   	$('<li role="button" class="cancel" ><a href="#">Restart</a></li>').css({"margin-left" : "230px"}).insertBefore('.next').click(function(){
	   		performAction(this, '/plugin/importwizard');
	   	});
	 
	   	//Remove bwizard default eventhandlers and add our own eventhandlers	
	   	
	   	$('.next').unbind('click').click(function(){
	   		<#if wizard.lastPage > 
	   			performAction(this, '/plugin/importwizard');
	   		<#else>
	   			performAction(this, '/plugin/importwizard/next');
	   		</#if>
	   	});
	   	
	   	$('.previous').unbind('click').click(function(){
	   		performAction(this, '/plugin/importwizard/previous');
	   	});
	   	
	   	<#if wizard.lastPage > 
	    	$('.next a').html('finish');
	    </#if>
	    
	   	<#if wizard.validationMessage! == "">
	   		$('.next').removeClass('disabled');
	   		
	   		<#if wizard.lastPage > 
	   			$('.cancel').addClass('disabled');
	   			$('.previous').addClass('disabled');
	   		</#if>
	   		
	   	<#else>
	   		$('.next').addClass('disabled');
	   	</#if>
	   	
	   	<#if wizard.firstPage && wizard.errorMessage! == "">
	   		$('.cancel').addClass('disabled');
	   	</#if>
	   	 	
	   	function performAction(btn, action) {
	   		if (!$(btn).hasClass('disabled')) {
	   			<#if wizard.currentPageIndex == 2>
	   				$('#spinner').modal('show');
	   			</#if>
	   			
	   			document.forms.importWizardForm.action = action;
	   			document.forms.importWizardForm.submit();
	   		}
	   	}
	</script>
			
		
	</body>
</html>
</#if>