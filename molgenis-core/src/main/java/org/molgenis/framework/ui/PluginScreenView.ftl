<#macro PluginView screen>
${screen.getHeader()}
${screen.getHtml()}
<#list screen.getChildren() as childscreen>
	<@layout childscreen/>
</#list>
</#macro>