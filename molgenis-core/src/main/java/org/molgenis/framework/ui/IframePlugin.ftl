<#-- include iframe that retrieves contents using Plugin.getIframeSrc -->
<#macro IframePlugin screen>
	<div class="formscreen" style="height:100%">
		<div class="form_header" id="${screen.name}">${screen.label}</div>
		<iframe src="${screen.iframeSrc}" frameborder="0" style="overflow:hidden;height:80vh;width:100%" height="80%" width="100%"></iframe>
	</div>
</#macro>