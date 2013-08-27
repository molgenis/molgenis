package org.molgenis.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class XmlMolgenisUiPluginTest
{
	private MolgenisPermissionService molgenisPermissionService;
	private MolgenisUiMenu molgenisUiMenu;

	@BeforeMethod
	public void setUp()
	{
		molgenisPermissionService = mock(MolgenisPermissionService.class);
		molgenisUiMenu = mock(MolgenisUiMenu.class);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void XmlMolgenisUiPlugin()
	{
		new XmlMolgenisUiPlugin(null, null, molgenisUiMenu);
	}

	@Test
	public void getId()
	{
		String pluginId = "pluginId";
		PluginType pluginType = new PluginType();
		pluginType.setId(pluginId);

		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(molgenisPermissionService, pluginType,
				molgenisUiMenu);
		assertEquals(xmlMolgenisUiPlugin.getId(), pluginId);
	}

	@Test
	public void getName()
	{
		String pluginId = "pluginId";
		String pluginName = "pluginName";
		PluginType pluginType = new PluginType();
		pluginType.setName(pluginId);
		pluginType.setLabel(pluginName);

		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(molgenisPermissionService, pluginType,
				molgenisUiMenu);
		assertEquals(xmlMolgenisUiPlugin.getName(), pluginName);
	}

	@Test
	public void getNameById()
	{
		String pluginId = "pluginId";
		PluginType pluginType = new PluginType();
		pluginType.setId(pluginId);

		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(molgenisPermissionService, pluginType,
				molgenisUiMenu);
		assertEquals(xmlMolgenisUiPlugin.getName(), pluginId);
	}

	@Test
	public void getType()
	{
		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(molgenisPermissionService, new PluginType(),
				molgenisUiMenu);
		assertEquals(xmlMolgenisUiPlugin.getType(), MolgenisUiMenuItemType.PLUGIN);
	}

	@Test
	public void getParentMenu()
	{
		assertEquals(
				new XmlMolgenisUiPlugin(molgenisPermissionService, new PluginType(), molgenisUiMenu).getParentMenu(),
				molgenisUiMenu);
	}

	@Test
	public void isAuthorized()
	{
		String type = "type";
		when(molgenisPermissionService.hasPermissionOnPlugin(type, Permission.READ)).thenReturn(true);
		PluginType pluginType = new PluginType();
		pluginType.setName(type);
		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(molgenisPermissionService, pluginType,
				molgenisUiMenu);
		assertTrue(xmlMolgenisUiPlugin.isAuthorized());
	}

	@Test
	public void isAuthorized_notAuthorized()
	{
		PluginType pluginType = new PluginType();
		pluginType.setName("type_notauthorized");
		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(molgenisPermissionService, new PluginType(),
				molgenisUiMenu);
		assertFalse(xmlMolgenisUiPlugin.isAuthorized());
	}
}
