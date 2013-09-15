package org.molgenis.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.molgenis.framework.ui.MolgenisPlugin;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class XmlMolgenisUiMenuTest
{
	private WebInvocationPrivilegeEvaluator webInvocationPrivilegeEvaluator;
	private Authentication authentication;

	@BeforeMethod
	public void setUp()
	{
		webInvocationPrivilegeEvaluator = mock(WebInvocationPrivilegeEvaluator.class);
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

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, menuType);
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

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, menuType);
		assertEquals(xmlMolgenisUiMenu.getName(), menuName);
	}

	@Test
	public void getNameByName()
	{
		String menuId = "menuId";
		MenuType menuType = new MenuType();
		menuType.setName(menuId);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, menuType);
		assertEquals(xmlMolgenisUiMenu.getName(), menuId);
	}

	@Test
	public void getType()
	{
		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, new MenuType());
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

		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + "plugin1", authentication))
				.thenReturn(true);
		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + "plugin2", authentication))
				.thenReturn(false);
		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + "plugin3", authentication))
				.thenReturn(true);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, menuType);
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

		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + "plugin1", authentication))
				.thenReturn(false);
		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + "plugin2", authentication))
				.thenReturn(true);
		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + "plugin3", authentication))
				.thenReturn(true);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, menuType);
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

		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + "plugin1", authentication))
				.thenReturn(false);
		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + "plugin3", authentication))
				.thenReturn(true);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, menuType);
		assertNull(xmlMolgenisUiMenu.getActiveItem());
	}

	@Test
	public void getParentMenu()
	{
		assertNull(new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, new MenuType()).getParentMenu());
		MolgenisUiMenu parentMenu = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, new MenuType());
		assertEquals(
				new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, new MenuType(), parentMenu).getParentMenu(),
				parentMenu);
	}

	@Test
	public void getBreadcrumb()
	{
		MolgenisUiMenu menu1 = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, new MenuType());
		MolgenisUiMenu menu2 = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, new MenuType(), menu1);
		MolgenisUiMenu menu3 = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, new MenuType(), menu2);

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

		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + pluginId, authentication))
				.thenReturn(true);

		MenuType menuType = new MenuType();
		menuType.getMenuOrPlugin().add(pluginType);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, menuType);
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

		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + plugin1Id, authentication))
				.thenReturn(false);
		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + plugin2Id, authentication))
				.thenReturn(true);

		MenuType menuType = new MenuType();
		menuType.getMenuOrPlugin().add(plugin1Type);
		menuType.getMenuOrPlugin().add(plugin2Type);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, menuType);
		assertTrue(xmlMolgenisUiMenu.isAuthorized());
	}

	@Test
	public void isAuthorized_notAuthorized()
	{
		PluginType pluginType = new PluginType();
		pluginType.setType("type");
		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + "something", authentication))
				.thenReturn(false);

		MenuType menuType = new MenuType();
		menuType.getMenuOrPlugin().add(pluginType);

		XmlMolgenisUiMenu xmlMolgenisUiMenu = new XmlMolgenisUiMenu(webInvocationPrivilegeEvaluator, menuType);
		assertFalse(xmlMolgenisUiMenu.isAuthorized());
	}
}
