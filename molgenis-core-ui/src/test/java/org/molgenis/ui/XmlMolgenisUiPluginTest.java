package org.molgenis.ui;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

@SuppressWarnings("deprecation")
public class XmlMolgenisUiPluginTest
{
	private MolgenisUiMenu molgenisUiMenu;

	@BeforeMethod
	public void setUp()
	{
		molgenisUiMenu = mock(MolgenisUiMenu.class);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void XmlMolgenisUiPlugin()
	{
		new XmlMolgenisUiPlugin(null, null);
	}

	@Test
	public void getId()
	{
		String pluginId = "pluginId";
		PluginType pluginType = new PluginType();
		pluginType.setId(pluginId);

		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(pluginType, molgenisUiMenu);
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

		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(pluginType, molgenisUiMenu);
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
		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(pluginType, molgenisUiMenu);
		assertEquals(xmlMolgenisUiPlugin.getName(), pluginName);
	}

	@Test
	public void getType()
	{
		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(new PluginType(), molgenisUiMenu);
		assertEquals(xmlMolgenisUiPlugin.getType(), MolgenisUiMenuItemType.PLUGIN);
	}

	@Test
	public void getParentMenu()
	{
		assertEquals(new XmlMolgenisUiPlugin(new PluginType(), molgenisUiMenu).getParentMenu(), molgenisUiMenu);
	}
}
