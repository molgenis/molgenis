package org.molgenis.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class XmlMolgenisUiMenuTest
{
	private MolgenisPermissionService molgenisPermissionService;

	@BeforeMethod
	public void setUp()
	{
		molgenisPermissionService = mock(MolgenisPermissionService.class);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void XmlMolgenisUiMenu()
	{
		new XmlMolgenisUiMenu(null, null);
	}

	@Test
	public void getId()
	{
		String menuId = "menuId";
		MenuType menuType = new MenuType();
		menuType.setName(menuId);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(molgenisPermissionService, menuType);
		assertEquals(xmlMolgenisUiMenu.getId(), menuId);
	}

	@Test
	public void getName()
	{
		String menuId = "menuId";
		String menuName = "menuName";
		MenuType menuType = new MenuType();
		menuType.setName(menuId);
		menuType.setLabel(menuName);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(molgenisPermissionService, menuType);
		assertEquals(xmlMolgenisUiMenu.getName(), menuName);
	}

	@Test
	public void getNameByName()
	{
		String menuId = "menuId";
		MenuType menuType = new MenuType();
		menuType.setName(menuId);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(molgenisPermissionService, menuType);
		assertEquals(xmlMolgenisUiMenu.getName(), menuId);
	}

	@Test
	public void getType()
	{
		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(molgenisPermissionService, new MenuType());
		assertEquals(xmlMolgenisUiMenu.getType(), MolgenisUiMenuItemType.MENU);
	}

	@Test
	public void getItems()
	{
		String menuId = "menuId";
		MenuType menuType = new MenuType();
		menuType.setName(menuId);

		PluginType plugin1Type = new PluginType();
		plugin1Type.setId("plugin1");
		plugin1Type.setName("plugin1");
		PluginType plugin2Type = new PluginType();
		plugin2Type.setId("plugin2");
		plugin2Type.setName("plugin2");
		PluginType plugin3Type = new PluginType();
		plugin3Type.setId("plugin3");
		plugin3Type.setName("plugin3");
		FormType form1Type = new FormType();
		form1Type.setName("form1");
		form1Type.setEntity("form1");
		FormType form2Type = new FormType();
		form2Type.setName("form2");
		form2Type.setEntity("form2");
		FormType form3Type = new FormType();
		form3Type.setName("form3");
		form3Type.setEntity("form3");
		MenuType menu1Type = new MenuType();
		menu1Type.setName("menu1");
		menu1Type.getFormOrMenuOrPlugin().add(plugin3Type);
		menu1Type.getFormOrMenuOrPlugin().add(form3Type);

		menuType.getFormOrMenuOrPlugin().add(plugin1Type);
		menuType.getFormOrMenuOrPlugin().add(plugin2Type);
		menuType.getFormOrMenuOrPlugin().add(form1Type);
		menuType.getFormOrMenuOrPlugin().add(form2Type);
		menuType.getFormOrMenuOrPlugin().add(menu1Type);

		when(molgenisPermissionService.hasPermissionOnPlugin("plugin1", Permission.READ)).thenReturn(true);
		when(molgenisPermissionService.hasPermissionOnPlugin("plugin2", Permission.READ)).thenReturn(false);
		when(molgenisPermissionService.hasPermissionOnPlugin("plugin3", Permission.READ)).thenReturn(true);
		when(molgenisPermissionService.hasPermissionOnEntity("form1", Permission.READ)).thenReturn(true);
		when(molgenisPermissionService.hasPermissionOnEntity("form2", Permission.READ)).thenReturn(false);
		when(molgenisPermissionService.hasPermissionOnEntity("form3", Permission.READ)).thenReturn(false);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(molgenisPermissionService, menuType);
		Iterator<MolgenisUiMenuItem> it = xmlMolgenisUiMenu.getItems().iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "plugin1");
		assertEquals(it.next().getName(), "form1");
		assertEquals(it.next().getName(), "menu1");
	}

	@Test
	public void getActiveItem()
	{
		String menuId = "menuId";
		MenuType menuType = new MenuType();
		menuType.setName(menuId);

		PluginType plugin1Type = new PluginType();
		plugin1Type.setId("plugin1");
		plugin1Type.setName("plugin1");
		PluginType plugin2Type = new PluginType();
		plugin2Type.setId("plugin2");
		plugin2Type.setName("plugin2");
		PluginType plugin3Type = new PluginType();
		plugin3Type.setId("plugin3");
		plugin3Type.setName("plugin3");
		MenuType menu1Type = new MenuType();
		menu1Type.setName("menu1");
		menu1Type.getFormOrMenuOrPlugin().add(plugin3Type);

		menuType.getFormOrMenuOrPlugin().add(plugin1Type);
		menuType.getFormOrMenuOrPlugin().add(menu1Type);
		menuType.getFormOrMenuOrPlugin().add(plugin2Type);

		when(molgenisPermissionService.hasPermissionOnPlugin("plugin1", Permission.READ)).thenReturn(false);
		when(molgenisPermissionService.hasPermissionOnPlugin("plugin2", Permission.READ)).thenReturn(true);
		when(molgenisPermissionService.hasPermissionOnPlugin("plugin3", Permission.READ)).thenReturn(true);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(molgenisPermissionService, menuType);
		assertEquals(xmlMolgenisUiMenu.getActiveItem().getName(), "plugin2");
	}

	@Test
	public void getActiveItem_noActiveItem()
	{
		String menuId = "menuId";
		MenuType menuType = new MenuType();
		menuType.setName(menuId);

		PluginType plugin1Type = new PluginType();
		plugin1Type.setId("plugin1");
		plugin1Type.setName("plugin1");
		PluginType plugin3Type = new PluginType();
		plugin3Type.setId("plugin3");
		plugin3Type.setName("plugin3");
		MenuType menu1Type = new MenuType();
		menu1Type.setName("menu1");
		menu1Type.getFormOrMenuOrPlugin().add(plugin3Type);

		menuType.getFormOrMenuOrPlugin().add(plugin1Type);
		menuType.getFormOrMenuOrPlugin().add(menu1Type);

		when(molgenisPermissionService.hasPermissionOnPlugin("plugin1", Permission.READ)).thenReturn(false);
		when(molgenisPermissionService.hasPermissionOnPlugin("plugin3", Permission.READ)).thenReturn(true);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(molgenisPermissionService, menuType);
		assertNull(xmlMolgenisUiMenu.getActiveItem());
	}

	@Test
	public void isAuthorized()
	{
		PluginType pluginType = new PluginType();
		String pluginName = "type";
		pluginType.setName(pluginName);
		when(molgenisPermissionService.hasPermissionOnPlugin(pluginName, Permission.READ)).thenReturn(true);

		MenuType menuType = new MenuType();
		menuType.getFormOrMenuOrPlugin().add(pluginType);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(molgenisPermissionService, menuType);
		assertTrue(xmlMolgenisUiMenu.isAuthorized());
	}

	@Test
	public void isAuthorized_partiallyAuthorized()
	{
		PluginType plugin1Type = new PluginType();
		String plugin1Name = "type1";
		plugin1Type.setName(plugin1Name);
		PluginType plugin2Type = new PluginType();
		String plugin2Name = "type2";
		plugin2Type.setName(plugin2Name);

		when(molgenisPermissionService.hasPermissionOnPlugin(plugin1Name, Permission.READ)).thenReturn(false);
		when(molgenisPermissionService.hasPermissionOnPlugin(plugin2Name, Permission.READ)).thenReturn(true);

		MenuType menuType = new MenuType();
		menuType.getFormOrMenuOrPlugin().add(plugin1Type);
		menuType.getFormOrMenuOrPlugin().add(plugin2Type);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(molgenisPermissionService, menuType);
		assertTrue(xmlMolgenisUiMenu.isAuthorized());
	}

	@Test
	public void isAuthorized_notAuthorized()
	{
		PluginType pluginType = new PluginType();
		pluginType.setType("type");
		when(molgenisPermissionService.hasPermissionOnPlugin("type", Permission.READ)).thenReturn(false);

		MenuType menuType = new MenuType();
		menuType.getFormOrMenuOrPlugin().add(pluginType);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(molgenisPermissionService, menuType);
		assertFalse(xmlMolgenisUiMenu.isAuthorized());
	}
}
