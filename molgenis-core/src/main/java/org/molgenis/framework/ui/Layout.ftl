<#--
DEPRECATED
 'screen' has a getViewMacroName() () method. This is the name of a macro used for layout.
Below, the macro forwards a screen to its appropriate layouter to get layouted.
-->
<#macro layout screen>
   <#-- deprecated for pluginModel-->
   <#--[${screen.name} <#if screen.viewTemplate?exists>${screen.viewTemplate}</#if> <#if screen.viewName?exists>${screen.viewName}</#if>]<br/> -->  
	<#if screen.viewTemplate?exists && screen.viewName?exists>
      <#include screen.getViewTemplate()>
      <#assign templateSource = "<@"+screen.getViewName() + " screen/>">
      <#assign inlineTemplate = templateSource?interpret>
      <@inlineTemplate screen />  
   <#else>
		${screen.render()}
	</#if>
</#macro>



