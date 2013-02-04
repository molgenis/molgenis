<#include "ScreenView.ftl">
<#include "FormViewJQ.ftl">
<#include "MenuView.ftl">
<#include "PluginView.ftl">
<#include "UserInterface.ftl">
<#include "ScreenCommand.ftl">
<#--
Each 'screen' has a getViewMacroName() () method. This is the name of a macro used for layout.
Below, the macro forwards a screen to its appropriate layouter to get layouted.
-->
<#macro layout screen>
<#assign templateSource = "<@"+screen.getViewName() + " screen/>">
<#assign inlineTemplate = templateSource?interpret>
<@inlineTemplate screen />
</#macro>
<#-- start with the 'main' screen, which is called 'application'-->
<@layout application/>
