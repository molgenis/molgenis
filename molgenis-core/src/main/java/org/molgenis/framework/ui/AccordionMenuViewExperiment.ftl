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
			<h3><a href="#">${item.getLabel()}</a></h3>
			<#if item.position?exists  && (item.position == "LEFT" || item.position == "DEFAULT") && item.getChildren()?size &gt; 1>
				<#--if the item is a left menu recurse-->
				<div id="${item.name}_tab_button" class="leftNav">
					<@MenuScreenLeft name=name screen=item submenu="true" />
				</div>
			<#else>
				<div id="${item.name}_tab_button">
					<a href="molgenis.do?__target=${__target}&select=${select}">${item.getLabel()}</a>
				</div>
			</#if>		
		</#list>
	</#if>
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
	<#--left menu is a two div structure with left navigation, right the information-->
	<#--nested menus are automatically merged-->
	<#--difficulty is nested menus, then the information should show the selection of subform-->
	
	<div style="width:100%">
		<div style="width:200px; float:left">
			<form name="${screen.getName()}" method="get" action="">
				<input type="hidden" name="__target" value="">
				<input type="hidden" name="select" value="">
				<div class="leftNav">
					<@MenuScreenLeft name=screen.getName() screen=screen submenu="false" />
				</div>
			</form>	
		</div>
		<div style="margin-left:200px">
			<@layout findFirstNonLeftMenu(screen)/>
		</div>
		<div style="clear:both"></div>
	</div>

<#elseif screen.position == "TOP_LEFT" || screen.position == "TOP_RIGHT">
	<#--tabs on top-->
	
	<div id="${screen.getName()}" class="menuscreen" <#if screen.position == "TOP_RIGHT">align="right"</#if>>
		<form name="${screen.getName()}" method="get" action="">
			<input type="hidden" name="__target" value="">
			<input type="hidden" name="select" value="">
	<#assign selectedItem = screen.getSelected()/>
	<#list screen.getVisibleChildren() as item>
		<#if item == selectedItem>
			<div id="${item.name}_tab_button" class="navigationSelected" onClick="document.forms.${screen.getName()}.__target.value='${screen.name}';document.forms.${screen.getName()}.select.value='${item.name}';document.forms.${screen.getName()}.submit();">${item.label}</div>
		<#else>
			<div id="${item.name}_tab_button" class="navigationNotSelected" onClick="document.forms.${screen.getName()}.__target.value='${screen.name}';document.forms.${screen.getName()}.select.value='${item.name}';document.forms.${screen.getName()}.submit();">${item.label}</div>
		</#if>
	</#list>
		</form>
	</div>
	
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

<!-- enable accordion -->
<script>
  $(document).ready(function() {
    $(".leftNav").accordion( {autoHeight:false} );
  });
</script>
  