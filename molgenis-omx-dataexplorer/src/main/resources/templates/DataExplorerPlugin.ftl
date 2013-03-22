<#-- include iframe that retrieves contents using DataExplorerController -->
<#macro DataExplorerPlugin screen>
	<div class="formscreen" style="height:100%">
		<div class="form_header" id="${screen.name}">${screen.label}</div>
		<iframe src="/plugin/dataexplorer" frameborder="0" style="overflow:hidden;height:100%;width:100%" height="100%" width="100%"></iframe>
	</div>
</#macro>