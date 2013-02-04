<#macro ImportWizard screen>
	<#assign wizard = screen.wizard>
	<#assign form = screen.name>
	
	<form method="post" enctype="multipart/form-data" name="${form}" action="">
		<!--needed in every form: to redirect the request to the right screen-->
		<input type="hidden" name="__target" value="${screen.name}">
		<!--needed in every form: to define the action. This can be set by the submit button-->
		<input type="hidden" name="__action">
		<!--need to be set to "true" in order to force a download-->
		<input type="hidden" name="__show">
		
		<div class="formscreen"">
			<div class="form_header" id="${screen.getName()}">${screen.label}</div>

			<div id="wizard" style="padding:16px">
				<ol>
					<#list wizard.pages as wizardPage>
						<li>${wizardPage.title}</li>
					</#list>
				</ol> 
				
				<#list wizard.pages as wizardPage>
					<div style="min-height: 180px">
						<p>		
							<#if wizard.errorMessage! != "" >
								<div class="alert alert-block alert-error">${wizard.errorMessage!}</div>
							</#if>	
							<#if wizard.validationMessage! != "" >
								<div class="alert alert-block alert-error">${wizard.validationMessage!}</div>
							</#if>	
							<#if wizard.successMessage! != "" >
								<div class="alert alert-block alert-success">${wizard.successMessage!}</div>
							</#if>	
							<#if wizardPage == wizard.currentPage && wizardPage.viewTemplate! != "">
								<#include wizardPage.viewTemplate />
							</#if>
						</p>
					</div>
				</#list>
				
			</div>
		</div>
	</form>	

	<div id="spinner" class="modal hide" style="width: 180px;margin: -200px 0 0 -90px;">
  		<div class="modal-header">
    		<h3>Importing...</h3>
 		 </div>
  		<div class="modal-body">
    		<div style="width: 32px;margin:10px auto"><img src="img/waiting-spinner.gif" /></div>
  		</div>
	</div>

	<script type="text/javascript">
		$("#wizard").bwizard({activeIndex: ${wizard.currentPageIndex}});
	   	$('.pager').css({"width" : "491px"});//Pager bar with previous/next buttons
	   	
	   //Add Cancel button
	   	$('<li role="button" class="cancel" ><a href="#">Cancel</a></li>').css({"margin-left" : "230px"}).insertBefore('.next').click(function(){
	   		performAction(this, 'cancel');
	   	});
	 
	   	//Remove bwizard default eventhandlers and add our own eventhandlers	
	   	
	   	$('.next').unbind('click').click(function(){
	   		<#if wizard.lastPage > 
	   			performAction(this, 'finish');
	   		<#else>
	   			performAction(this, 'next');
	   		</#if>
	   	});
	   	
	   	$('.previous').unbind('click').click(function(){
	   		performAction(this, 'previous');
	   	});
	   	
	   	<#if wizard.lastPage > 
	    	$('.next a').html('Finish');
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
	   			document.forms.${form}.__action.value = action;
	   			document.forms.${form}.submit();
	   		}
	   	}
	</script>
	
</#macro>
