<#macro Settings screen>
<#assign model = screen.report>
<!-- normally you make one big form for the whole plugin-->
<form method="post" enctype="multipart/form-data" name="${screen.name}" action="">
	<!--needed in every form: to redirect the request to the right screen-->
	<input type="hidden" name="__target" value="${screen.name}">
	<!--needed in every form: to define the action. This can be set by the submit button-->
	<input type="hidden" name="__action">
	<!--need to be set to "true" in order to force a download-->
	<input type="hidden" name="__show">
	
	<#assign bgColor = "#ffffff">
	<#assign borderStyle = "solid">
	<#assign borderWidthSpacing = "1px">
	<#assign picSize = "15">
	<#assign padding = "5px">
	
	<#assign style1 = "style=\"padding: ${padding}; border-width: ${borderWidthSpacing}; border-spacing: ${borderWidthSpacing}; border-style: ${borderStyle}; border-color: #800000; background-color: ${bgColor};\"">
	<#assign style2 = "style=\"padding: ${padding}; border-width: ${borderWidthSpacing}; border-spacing: ${borderWidthSpacing}; border-style: ${borderStyle}; border-color: #0000FF; background-color: ${bgColor};\"">
	<#assign styleWhite = "style=\"padding: ${padding}; border-width: ${borderWidthSpacing}; border-spacing: ${borderWidthSpacing}; border-style: ${borderStyle}; border-color: #FFFFFF; background-color: ${bgColor};\"">
	<#assign styleGreen = "style=\"padding: ${padding}; border-width: ${borderWidthSpacing}; border-spacing: ${borderWidthSpacing}; border-style: ${borderStyle}; border-color: #00FF00; background-color: ${bgColor};\"">
	<#assign styleBlack = "style=\"padding: ${padding}; border-width: ${borderWidthSpacing}; border-spacing: ${borderWidthSpacing}; border-style: ${borderStyle}; border-color: #000000; background-color: ${bgColor};\"">
	
	
	<#assign green = "style=\"padding: ${padding}; border-width: ${borderWidthSpacing}; border-spacing: ${borderWidthSpacing}; border-style: ${borderStyle}; border-color: #00FF00; background-color: #00FF00;\"">
	<#assign orange = "style=\"padding: ${padding}; border-width: ${borderWidthSpacing}; border-spacing: ${borderWidthSpacing}; border-style: ${borderStyle}; border-color: #FF8040; background-color: #FF8040;\"">
	<#assign red = "style=\"padding: ${padding}; border-width: ${borderWidthSpacing}; border-spacing: ${borderWidthSpacing}; border-style: ${borderStyle}; border-color: #FF0000; background-color: #FF0000;\"">
	
	<#assign center = "align=\"center\"">
	<#assign courier = "<font face=\"Courier New, Courier, mono, serif\">">	
	<#assign endFont = "</font>">
	
	<#assign greenBg = "<font style=\"background-color: #52D017\">"> <#--successmess: 52D017-->
	<#assign redBg = "<font style=\"background-color: red; color: white; font-weight:bold\">">
		
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


<h1>System settings</h1>


File storage property status:<br>
<font style="font-size:medium; font-family: Courier, 'Courier New', monospace">
<#if model.fileStoragePropsPresent == true>
	<font style="color: green">Properties are set</font>
<#elseif model.fileStoragePropsPresent == false>
	<font style="color: red">Properties are not set</font>
<#else>
	<font style="color: red">ERROR: Properties cannot be queried</font>
</#if>
</font>

<br><br>

System file storage path:<br>
<font style="font-size:medium; font-family: Courier, 'Courier New', monospace">
<#if model.fileStoragePropsPresent == true>
	<#if model.fileStorage?exists>
		<font style="color: blue">${model.getFileStorage().getAbsolutePath()}</font>
	<#else>
		<font style="color: red">NULL</font>
	</#if>
	
<#elseif model.fileStoragePropsPresent == false>
	<font style="color: red">N/A</font>
<#else>
	<font style="color: red">N/A</font>
</#if>
</font>

<br><br>

<input type="submit" value="Delete path" onclick="if (confirm('You are about to remove the file path reference. Are you sure?')) { document.forms.${screen.name}.__action.value = 'deleteFileDirPath'; document.forms.${screen.name}.submit(); } else { return false; }"/>

<br><br>

Set your file storage root path:<br>
<input type="text" size="30" style="border:2px solid black; color:blue; display:inline; font-size:medium; font-family: Courier, 'Courier New', monospace" id="inputBox" name="fileDirPath" value="./data" onkeypress="if(window.event.keyCode==13){document.forms.${screen.name}.__action.value = 'setFileDirPath';}">

<input type="submit" id="filestorage_setpath" value="Set path" onclick="document.forms.${screen.name}.__action.value = 'setFileDirPath'; document.forms.${screen.name}.submit();"/>

<br><br>
Directory exists?
<font style="font-size:medium; font-family: Courier, 'Courier New', monospace">
	<#if model.folderExists?exists><#if model.folderExists == true><font style="color: blue">YES</font><#else><font style="color: blue">NO</font></#if><#else><font style="color: red">N/A</font></#if>
</font>
<br>
Directory has contents?
<font style="font-size:medium; font-family: Courier, 'Courier New', monospace">
	<#if model.folderHasContent?exists><#if model.folderHasContent == true><font style="color: blue">YES</font><#else><font style="color: blue">NO</font></#if><#else><font style="color: red">N/A</font></#if>
</font>

<br><br>
Press 'validate' to Find out if this directory is a valid file path on the system,
attempt to create folders if the path does not exist,
and test if app is allowed to write and read files from this directory.
<br><br>
<input type="submit" value="Validate" onclick="document.forms.${screen.name}.__action.value = 'validate'; document.forms.${screen.name}.submit();"/>

<br><br>

Validation status: 
<font style="font-size:medium; font-family: Courier, 'Courier New', monospace">
	<#if model.verified?exists><#if model.verified == true><font style="color: green">VALIDATED</font><#else><font style="color: red">UNVALIDATED</font></#if><#else><font style="color: red">N/A</font></#if>
</font>


<#--end of your plugin-->	
			</div>
		</div>
	</div>
</form>
</#macro>
