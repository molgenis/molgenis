<#macro PubMedTest screen>
<!-- normally you make one big form for the whole plugin-->
<form method="post" enctype="multipart/form-data" name="${screen.name}" action="">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${screen.name}">
	<!--needed in every form: to define the action. This can be set by the submit button-->
	<input type="hidden" name="__action">
	<!--need to be set to "true" in order to force a download-->
	<input type="hidden" name="__show">
	
	

	
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
		
<#if screen.myModel?exists>
	<#assign modelExists = true>
	<#assign model = screen.myModel>
<#else>
	No model. An error has occurred.
	<#assign modelExists = false>
</#if>

	


PubMed Identifier: <input type="text" name="Pubmed" /><br />
<input type="submit" value="Submit" onclick="document.forms.${screen.name}.__action.value = 'query'; document.forms.${screen.name}.submit();" />
<input type="button" onclick="document.forms.${screen.name}.reset()" value="Reset form" />

<#if model.pubmed?exists>

<#if model.pubmed['abstract']?exists>
<ul>Abstract:${model.pubmed['abstract']}</ul>

<#else>
<p>Abstract: NULL <p>

</#if>

<#if model.pubmed['doi']?exists>
<ul>doi:${model.pubmed['doi']}</ul>

<#else>
<p>doi: NULL <p>

</#if>

<#if model.pubmed['title']?exists>
<ul>Title:${model.pubmed['title']}</ul>

<#else>
<p>title: NULL <p>

</#if>

<#if model.pubmed['authors']?exists>
<ul>Authors:${model.pubmed['authors']}</ul>

<#else>
<p>authors: NULL <p>

</#if>

<#if model.pubmed['pubmed']?exists>
<ul>PubMed:${model.pubmed['pubmed']}</ul>

<#else>
<p>PubMed: NULL <p>

</#if>

</#if>

    

     

	</div>
</form>
</#macro>
