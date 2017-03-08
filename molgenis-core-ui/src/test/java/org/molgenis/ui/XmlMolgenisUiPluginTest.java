package org.molgenis.ui;

import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@SuppressWarnings("deprecation")
public class XmlMolgenisUiPluginTest
{
	private MolgenisPermissionService molgenisPermissionService;
	private Authentication authentication;
	private MolgenisUiMenu molgenisUiMenu;

	@BeforeMethod
	public void setUp()
	{
		molgenisPermissionService = mock(MolgenisPermissionService.class);
		authentication = mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		molgenisUiMenu = mock(MolgenisUiMenu.class);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void XmlMolgenisUiPlugin()
	{
		new XmlMolgenisUiPlugin(null, null, null);
	}

	@Test
	public void getId()
	{
		String pluginId = "pluginId";
		PluginType pluginType = new PluginType();
		pluginType.setId(pluginId);

		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(pluginType, molgenisUiMenu,
				molgenisPermissionService);
		assertEquals(xmlMolgenisUiPlugin.getId(), pluginId);
	}

	@Test
	public void getName()
	{
		String pluginId = "pluginId";
		String pluginName = "pluginName";
		PluginType pluginType = new PluginType();
		pluginType.setName(pluginId);
		pluginType.setName(pluginName);

		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(pluginType, molgenisUiMenu,
				molgenisPermissionService);
		assertEquals(xmlMolgenisUiPlugin.getName(), pluginName);
	}

	@Test
	public void getNameById()
	{
		String pluginId = "pluginId";
		String pluginName = "pluginName";
		PluginType pluginType = new PluginType();
		pluginType.setId(pluginId);
		pluginType.setName(pluginName);
		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(pluginType, molgenisUiMenu,
				molgenisPermissionService);
		assertEquals(xmlMolgenisUiPlugin.getName(), pluginName);
	}

	@Test
	public void getType()
	{
		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(new PluginType(), molgenisUiMenu,
				molgenisPermissionService);
		assertEquals(xmlMolgenisUiPlugin.getType(), MolgenisUiMenuItemType.PLUGIN);
	}

	@Test
	public void getParentMenu()
	{
		assertEquals(
				new XmlMolgenisUiPlugin(new PluginType(), molgenisUiMenu, molgenisPermissionService).getParentMenu(),
				molgenisUiMenu);
	}

	@Test
	public void isAuthorized()
	{
		String id = "plugin1";
		String type = "type";
		PluginType pluginType = new PluginType();
		pluginType.setName(type);
		pluginType.setId(id);
		when(molgenisPermissionService.hasPermissionOnPlugin(id, Permission.READ)).thenReturn(true);
		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(pluginType, molgenisUiMenu,
				molgenisPermissionService);
		assertTrue(xmlMolgenisUiPlugin.isAuthorized());
	}

	@Test
	public void isAuthorized_notAuthorized()
	{
		PluginType pluginType = new PluginType();
		pluginType.setName("type_notauthorized");
		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(new PluginType(), molgenisUiMenu,
				molgenisPermissionService);
		assertFalse(xmlMolgenisUiPlugin.isAuthorized());
	}
}
