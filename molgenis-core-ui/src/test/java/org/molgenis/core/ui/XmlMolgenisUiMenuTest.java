package org.molgenis.core.ui;

import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.web.UiMenu;
import org.molgenis.web.UiMenuItem;
import org.molgenis.web.UiMenuItemType;
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
	private UserPermissionEvaluator permissionService;
	private Authentication authentication;

	@BeforeMethod
	public void setUp()
	{
		permissionService = mock(UserPermissionEvaluator.class);
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

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, permissionService);
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

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, permissionService);
		assertEquals(xmlMolgenisUiMenu.getName(), menuName);
	}

	@Test
	public void getNameByName()
	{
		String menuId = "menuId";
		MenuType menuType = new MenuType();
		menuType.setName(menuId);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, permissionService);
		assertEquals(xmlMolgenisUiMenu.getName(), menuId);
	}

	@Test
	public void getType()
	{
		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(new MenuType(), permissionService);
		assertEquals(xmlMolgenisUiMenu.getType(), UiMenuItemType.MENU);
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

		when(permissionService.hasPermission(new PluginIdentity("plugin1"), PluginPermission.READ)).thenReturn(true);
		when(permissionService.hasPermission(new PluginIdentity("plugin2"), PluginPermission.READ)).thenReturn(false);
		when(permissionService.hasPermission(new PluginIdentity("plugin3"), PluginPermission.READ)).thenReturn(true);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, permissionService);
		Iterator<UiMenuItem> it = xmlMolgenisUiMenu.getItems().iterator();
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

		when(permissionService.hasPermission(new PluginIdentity("plugin1"), PluginPermission.READ)).thenReturn(false);
		when(permissionService.hasPermission(new PluginIdentity("plugin2"), PluginPermission.READ)).thenReturn(true);
		when(permissionService.hasPermission(new PluginIdentity("plugin3"), PluginPermission.READ)).thenReturn(true);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, permissionService);
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

		when(permissionService.hasPermission(new PluginIdentity("plugin1"), PluginPermission.READ)).thenReturn(false);
		when(permissionService.hasPermission(new PluginIdentity("plugin3"), PluginPermission.READ)).thenReturn(true);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, permissionService);
		assertNull(xmlMolgenisUiMenu.getActiveItem());
	}

	@Test
	public void getParentMenu()
	{
		assertNull(new XmlMolgenisUiMenu(new MenuType(), permissionService).getParentMenu());
		UiMenu parentMenu = new XmlMolgenisUiMenu(new MenuType(), permissionService);
		assertEquals(new XmlMolgenisUiMenu(new MenuType(), parentMenu, permissionService).getParentMenu(), parentMenu);
	}

	@Test
	public void getBreadcrumb()
	{
		UiMenu menu1 = new XmlMolgenisUiMenu(new MenuType(), permissionService);
		UiMenu menu2 = new XmlMolgenisUiMenu(new MenuType(), menu1, permissionService);
		UiMenu menu3 = new XmlMolgenisUiMenu(new MenuType(), menu2, permissionService);

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

		when(permissionService.hasPermission(new PluginIdentity(pluginId), PluginPermission.READ)).thenReturn(true);

		MenuType menuType = new MenuType();
		menuType.getMenuOrPlugin().add(pluginType);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, permissionService);
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

		when(permissionService.hasPermission(new PluginIdentity(plugin1Id), PluginPermission.READ)).thenReturn(false);
		when(permissionService.hasPermission(new PluginIdentity(plugin2Id), PluginPermission.READ)).thenReturn(true);

		MenuType menuType = new MenuType();
		menuType.getMenuOrPlugin().add(plugin1Type);
		menuType.getMenuOrPlugin().add(plugin2Type);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, permissionService);
		assertTrue(xmlMolgenisUiMenu.isAuthorized());
	}

	@Test
	public void isAuthorized_notAuthorized()
	{
		PluginType pluginType = new PluginType();
		pluginType.setId("something");
		when(permissionService.hasPermission(new PluginIdentity("something"), PluginPermission.READ)).thenReturn(false);

		MenuType menuType = new MenuType();
		menuType.getMenuOrPlugin().add(pluginType);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(menuType, permissionService);
		assertFalse(xmlMolgenisUiMenu.isAuthorized());
	}
}
