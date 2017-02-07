package org.molgenis.ui;

import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@SuppressWarnings("deprecation")
public class XmlMolgenisUiMenuTest
{
	private MolgenisPermissionService molgenisPermissionService;
	private Authentication authentication;

	@BeforeMethod
	public void setUp()
	{
		molgenisPermissionService = mock(MolgenisPermissionService.class);
		authentication = mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
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

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, molgenisPermissionService);
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

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, molgenisPermissionService);
		assertEquals(xmlMolgenisUiMenu.getName(), menuName);
	}

	@Test
	public void getNameByName()
	{
		String menuId = "menuId";
		MenuType menuType = new MenuType();
		menuType.setName(menuId);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, molgenisPermissionService);
		assertEquals(xmlMolgenisUiMenu.getName(), menuId);
	}

	@Test
	public void getType()
	{
		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(new MenuType(), molgenisPermissionService);
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
		MenuType menu1Type = new MenuType();
		menu1Type.setName("menu1");
		menu1Type.getMenuOrPlugin().add(plugin3Type);

		menuType.getMenuOrPlugin().add(plugin1Type);
		menuType.getMenuOrPlugin().add(plugin2Type);
		menuType.getMenuOrPlugin().add(menu1Type);

		when(molgenisPermissionService.hasPermissionOnPlugin("plugin1", Permission.READ)).thenReturn(true);
		when(molgenisPermissionService.hasPermissionOnPlugin("plugin2", Permission.READ)).thenReturn(false);
		when(molgenisPermissionService.hasPermissionOnPlugin("plugin3", Permission.READ)).thenReturn(true);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, molgenisPermissionService);
		Iterator<MolgenisUiMenuItem> it = xmlMolgenisUiMenu.getItems().iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "plugin1");
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
		menu1Type.getMenuOrPlugin().add(plugin3Type);

		menuType.getMenuOrPlugin().add(plugin1Type);
		menuType.getMenuOrPlugin().add(menu1Type);
		menuType.getMenuOrPlugin().add(plugin2Type);

		when(molgenisPermissionService.hasPermissionOnPlugin("plugin1", Permission.READ)).thenReturn(false);
		when(molgenisPermissionService.hasPermissionOnPlugin("plugin2", Permission.READ)).thenReturn(true);
		when(molgenisPermissionService.hasPermissionOnPlugin("plugin3", Permission.READ)).thenReturn(true);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, molgenisPermissionService);
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
		menu1Type.getMenuOrPlugin().add(plugin3Type);

		menuType.getMenuOrPlugin().add(plugin1Type);
		menuType.getMenuOrPlugin().add(menu1Type);

		when(molgenisPermissionService.hasPermissionOnPlugin("plugin1", Permission.READ)).thenReturn(false);
		when(molgenisPermissionService.hasPermissionOnPlugin("plugin3", Permission.READ)).thenReturn(true);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, molgenisPermissionService);
		assertNull(xmlMolgenisUiMenu.getActiveItem());
	}

	@Test
	public void getParentMenu()
	{
		assertNull(new XmlMolgenisUiMenu(new MenuType(), molgenisPermissionService).getParentMenu());
		MolgenisUiMenu parentMenu = new XmlMolgenisUiMenu(new MenuType(), molgenisPermissionService);
		assertEquals(new XmlMolgenisUiMenu(new MenuType(), parentMenu, molgenisPermissionService).getParentMenu(),
				parentMenu);
	}

	@Test
	public void getBreadcrumb()
	{
		MolgenisUiMenu menu1 = new XmlMolgenisUiMenu(new MenuType(), molgenisPermissionService);
		MolgenisUiMenu menu2 = new XmlMolgenisUiMenu(new MenuType(), menu1, molgenisPermissionService);
		MolgenisUiMenu menu3 = new XmlMolgenisUiMenu(new MenuType(), menu2, molgenisPermissionService);

		assertEquals(menu1.getBreadcrumb(), Arrays.asList(menu1));
		assertEquals(menu2.getBreadcrumb(), Arrays.asList(menu1, menu2));
		assertEquals(menu3.getBreadcrumb(), Arrays.asList(menu1, menu2, menu3));
	}

	@Test
	public void isAuthorized()
	{
		PluginType pluginType = new PluginType();
		String pluginId = "plugin1";
		String pluginName = "type";
		pluginType.setName(pluginName);
		pluginType.setId(pluginId);

		when(molgenisPermissionService.hasPermissionOnPlugin(pluginId, Permission.READ)).thenReturn(true);

		MenuType menuType = new MenuType();
		menuType.getMenuOrPlugin().add(pluginType);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, molgenisPermissionService);
		assertTrue(xmlMolgenisUiMenu.isAuthorized());
	}

	@Test
	public void isAuthorized_partiallyAuthorized()
	{
		PluginType plugin1Type = new PluginType();
		String plugin1Name = "type1";
		String plugin1Id = "plugin1";
		plugin1Type.setName(plugin1Name);
		plugin1Type.setId(plugin1Id);
		PluginType plugin2Type = new PluginType();
		String plugin2Name = "type2";
		String plugin2Id = "plugin2";
		plugin2Type.setName(plugin2Name);
		plugin2Type.setId(plugin2Id);

		when(molgenisPermissionService.hasPermissionOnPlugin(plugin1Id, Permission.READ)).thenReturn(false);
		when(molgenisPermissionService.hasPermissionOnPlugin(plugin2Id, Permission.READ)).thenReturn(true);

		MenuType menuType = new MenuType();
		menuType.getMenuOrPlugin().add(plugin1Type);
		menuType.getMenuOrPlugin().add(plugin2Type);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, molgenisPermissionService);
		assertTrue(xmlMolgenisUiMenu.isAuthorized());
	}

	@Test
	public void isAuthorized_notAuthorized()
	{
		PluginType pluginType = new PluginType();
		when(molgenisPermissionService.hasPermissionOnPlugin("something", Permission.READ)).thenReturn(false);

		MenuType menuType = new MenuType();
		menuType.getMenuOrPlugin().add(pluginType);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, molgenisPermissionService);
		assertFalse(xmlMolgenisUiMenu.isAuthorized());
	}
}
