<#macro plugins_browser_GenomeBrowserPlugin screen>
<!-- normally you make one big form for the whole plugin-->
<form method="post" enctype="multipart/form-data" name="${screen.name}" action="">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${screen.name}">
	<!--needed in every form: to define the action. This can be set by the submit button-->
	<input type="hidden" name="__action">
	
<!-- this shows a title and border -->
	<div class="formscreen">
		<div class="form_header" id="${screen.getName()}">
		${screen.label}
		</div>
		
		<#--optional: mechanism to show messages-->
		<#list screen.getMessages() as message>
			<#if message.success>
		<p class="successmessage">${message.text}</p>
			<#else>
		<p class="errormessage">${message.text}</p>
			</#if>
		</#list>
		
		<div class="screenbody">
			<div class="screenpadding">	
<#--begin your plugin-->


<h3> Embedded genome browser - <a target="_blank" href="http://localhost/jbrowse2/">open it in a new window</a></h3>
<div align="middle">
	<iframe name="ucsc_iframe" height="500px" width="1000px" src="http://localhost/jbrowse2/"></iframe>

</div>



<#--end of your plugin-->	
			</div>
		</div>

</form>
</#macro>
