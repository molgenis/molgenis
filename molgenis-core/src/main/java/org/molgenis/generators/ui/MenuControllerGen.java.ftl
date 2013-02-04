<#--#####################################################################
Generate Table Data Gateway
* One table per concrete class
* One table per class hierarchy root (ensures id's and types)
* Associations map onto the hierarchy root
#####################################################################-->
<#include "GeneratorHelper.ftl">
<#assign screenpackage = menu.getPackageName() />
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* File:        ${Name(model)}/screen/${menu.getName()}.java
 * Copyright:   GBIC 2000-${year?c}, all rights reserved
 * Date:        ${date}
 * 
 * generator:   ${generator} ${version}
 *
 * 
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */


package ${package};

import org.molgenis.framework.ui.MenuModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.MenuController;

/**
 *
 */
public class ${Name(menu.className)}Menu extends MenuController
{
	private static final long serialVersionUID = 1L;
	
	public ${Name(menu.className)}Menu (ScreenController<?> parent)
	{
		super( "${menu.getVelocityName()}", parent );
		getModel().setLabel("${menu.label}");
		getModel().setPosition(MenuModel.Position.${menu.position?upper_case});
		
		//add ui elements
<#list menu.getChildren() as subscreen>
<#assign screentype = Name(subscreen.getType().toString()?lower_case) />
<#if screentype == "Form"><#assign screentype = "FormController"/></#if>
		new ${package}.${JavaName(subscreen)}${screentype}(this);
</#list>			
	}	
}


