<#include "GeneratorHelper.ftl">
<#assign screenpackage = plugin.getPackageName() />
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* File:        ${Name(model)}/screen/${plugin.getName()}.java
 * Copyright:   GBIC 2000-${year?c}, all rights reserved
 * Date:        ${date}
 * 
 * generator:   ${generator} ${version}
 *
 * 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */

package ${package};

import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.util.Entity;

/**
 *
 */
public class ${JavaName(plugin.className)}Plugin extends ${plugin.getPluginType()}
{
	private static final long serialVersionUID = 1L;
<#if plugin.pluginType == "org.molgenis.framework.screen.plugin.PluginScreen">	
    public ${JavaName(plugin.className)}Plugin(ScreenController<?>  parent)
	{
		super("${plugin.getVelocityName()}", parent);
		
		getModel().setLabel("${plugin.label}");
		
		<#list plugin.getChildren() as subscreen>
		<#assign screentype = Name(subscreen.getType().toString()?lower_case) />
		<#if screentype == "Form"><#assign screentype = "FormModel"/></#if>
		new ${package}.${JavaName(subscreen)}${screentype}(this);
		</#list>	
	}
<#else>
	public ${JavaName(plugin.getClassName())}Plugin(ScreenController<?> parent)
	{
		super("${plugin.getVelocityName()}", parent);
		
		getModel().setLabel("${plugin.label}");
		
		<#list plugin.getChildren() as subscreen>
		<#assign screentype = Name(subscreen.getType().toString()?lower_case) />
		<#if screentype == "Form"><#assign screentype = "FormModel"/></#if>
		new ${package}.${JavaName(subscreen)}${screentype}(this);
		</#list>			
	}
</#if>
}