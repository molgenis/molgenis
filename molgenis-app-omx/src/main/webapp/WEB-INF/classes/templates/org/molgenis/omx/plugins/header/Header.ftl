<#macro plugins_header_Header screen>
<#assign model = screen.myModel>
<div id="header" style="margin-top: 5px; margin-bottom: 10px;">
	<a href="/lifelines/">
		<img src="${model.hrefLogo?html}">
	</a>
</div>
</#macro>
