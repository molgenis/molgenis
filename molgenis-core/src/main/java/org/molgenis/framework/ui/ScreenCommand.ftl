<#--<@molgenis_header />-->
<#if model.inputs?exists >
<#--<body>-->
<div class="formscreen">
	<form action="" method="post" enctype="multipart/form-data" name="${model.name}" id="${model.name}">
	<p class="form_header">${model.label}</p>
	<#if model.messages?exists><#list model.getMessages() as message>
		<#if message.success>
	<p class="successmessage">${message.text}</p>
		<#else>
	<p class="errormessage">${message.text}</p>
		</#if>
	</#list></#if>	
	<table>
			<input type="hidden" name="__target" value="${model.target}"/>
			<input type="hidden" name="__action" value="${model.name}"/> 
			<input type="hidden" name="__command" value="${model.name}"/> 
			<input type="hidden" name="__show"/> 
	<#assign requiredcount = 0 />
	<#assign required = "" />
	<#list model.getInputs() as input>
		<#if !input.isHidden()>
			<tr>
				<td title="${input.description}"><label>${input.label}<#if !input.isNillable()  && !input.isReadonly()> *</#if></label></td>
				<td>${input.toHtml()}</td>
			</tr>
		<#else>
			${input.toHtml()}
		</#if>		
		<#if input.uiToolkit=='ORIGINAL' && !input.isNillable() && !input.isHidden() && !input.isReadonly()>
			<#if requiredcount &gt; 0><#assign required = required + "," /></#if>
			<#assign required = required + "document.forms.molgenis_popup."+ input.id />
			<#assign requiredcount = requiredcount + 1 />
		</#if>
	</#list>
	
<script language="JavaScript" type="text/javascript">
var molgenis_required = new Array(${required});
</script>
	</table>
	<p align="left">
<#list model.getActions() as input>
	${input.toHtml()}
</#list>
	</p>
	</form>
	<script>
		$("#molgenis_popup").validate();
	</script>
<#--</body>-->
</#if>
</div>
<#--</body>-->