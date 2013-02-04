<#include "Layout.ftl"/>
<#function findFirstNonLeftMenu screen>
	<#if screen.getSelected()?exists && screen.getSelected().position?exists && screen.getSelected().position == "LEFT">
		<#return findFirstNonLeftMenu(screen.getSelected())>
	<#else>
		<#return screen.getSelected()>
	</#if>
</#function>

<#--get the first selected item that is not a menu-->
<#function MenuScreenLeftSelectedItem screen>
	<#assign selectedItem = screen.getSelected()/>
	<#if selectedItem.getClass().getSuperclass().getSimpleName() == "MenuView">
		<#return selectedItem.getSelected()>
	<#else>
		<#return selectedItem>
	</#if>
</#function>

<#macro MenuScreenLeft name screen submenu>
	<#assign selectedItem = screen.getSelected()/>
    <#if screen.visibleChildren?exists>
    <#list screen.getVisibleChildren() as item>
		<#assign __target = screen.getName() />
		<#assign select = item.getName() />
		<#--if the item is a left menu recurse-->
		<#if item == selectedItem> 
			<div id="${item.name}_tab_button" class="leftNavigationSelected" onClick="document.forms.${name}.__target.value='${__target}';document.forms.${name}.select.value='${select}';document.forms.${name}.submit();">
				${item.getLabel()}
			</div>
			<#if item.position?exists  && (item.position == "LEFT" || item.position == "DEFAULT") && item.getChildren()?size &gt; 1>
				<div class="leftNavigationSubmenu">
					<@MenuScreenLeft name=name screen=item submenu="true" />
				</div>
			</#if>		
		<#else>
			<div  id="${item.name}_tab_button" class="leftNavigationNotSelected" onClick="document.forms.${name}.__target.value='${__target}';document.forms.${name}.select.value='${select}';document.forms.${name}.submit();">${item.getLabel()}</div>
		</#if>
	</#list></#if>
</#macro>

<#function hasParentForm screen>
	<#if screen.getParent()?exists>
		<#if screen.getParent().getClass().getSuperclass().getSimpleName() != "MenuView">
			<#return true/>
		<#else>
			<#return hasParentForm(screen.getParent())/>
		</#if>
	</#if>
	<#return false>
</#function>

<!-- layouting Menu '${screen.name}'-->
<#if screen.position == "LEFT">
<#--left menu is a two column table with left navigation, right the information-->
<#--nested menus are automatically merged-->
<#--difficulty is nested menus, then the information should show the selection of subform-->

<table class="leftNavigation">
	<tr>
		<td>
			<form name="${screen.getName()}" method="get" action="">
				<input type="hidden" name="__target" value="">
				<input type="hidden" name="select" value="">
				<div class="leftNavigationMenu">
					<@MenuScreenLeft name=screen.getName() screen=screen submenu="false" />
				</div>
			</form>	
		</td>
		<td valign="top" align="left" width="100%">
			<@layout findFirstNonLeftMenu(screen)/>
		
			<#--if screen.getSelected().getViewName() == "MenuView" >
				<@layout screen.getSelected().getSelected() />
			<#else>
				<@layout screen.getSelected() />
			</#if-->
		</td>
	</tr>
</table>

<#elseif screen.position == "TOP_LEFT" || screen.position == "TOP_RIGHT">
<#--tabs on top-->

<div id="${screen.getName()}" class="menuscreen" <#if screen.position == "TOP_RIGHT">align="right"</#if>>
	<form name="${screen.getName()}" method="get" action="">
		<input type="hidden" name="__target" value="">
		<input type="hidden" name="select" value="">
<#if screen.getSelected()?exists>
<#assign selectedItem = screen.getSelected()/>
<#list screen.getVisibleChildren() as item>
<#if item == selectedItem>
	<div id="${item.name}_tab_button" class="navigationSelected" onClick="document.forms.${screen.getName()}.__target.value='${screen.name}';document.forms.${screen.getName()}.select.value='${item.name}';document.forms.${screen.getName()}.submit();">${item.label}</div>
<#else>
	<div id="${item.name}_tab_button" class="navigationNotSelected" onClick="document.forms.${screen.getName()}.__target.value='${screen.name}';document.forms.${screen.getName()}.select.value='${item.name}';document.forms.${screen.getName()}.submit();">${item.label}</div>
</#if>
</#list>
</#if>
	</form>
</div>

<#if screen.getSelected()?exists>
<#assign subscreen = screen.getSelected()/>
<#if subscreen.position?exists  && (subscreen.position == "TOP_LEFT" || subscreen.position == "TOP_RIGHT")>
<div class="formscreen">
<div class="form_header" id="${screen.getName()}">
	<table width="100%">
		<tr>
			<td colspan="3" class="form_title">
				${screen.getSelected().label}
			</td>
		</tr>
	</table>
</div>	
<br>
<div>
	<@layout subscreen />
</div>
</div>	
<#--no directly underlying menu-->
<#else>
	<@layout screen.getSelected() />
</#if>
</#if>
</#if>