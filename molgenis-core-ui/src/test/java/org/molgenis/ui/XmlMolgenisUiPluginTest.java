package org.molgenis.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.molgenis.framework.ui.MolgenisPlugin;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class XmlMolgenisUiPluginTest
{
	private WebInvocationPrivilegeEvaluator webInvocationPrivilegeEvaluator;
	private Authentication authentication;
	private MolgenisUiMenu molgenisUiMenu;

	@BeforeMethod
	public void setUp()
	{
		webInvocationPrivilegeEvaluator = mock(WebInvocationPrivilegeEvaluator.class);
		authentication = mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
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

		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(webInvocationPrivilegeEvaluator, pluginType,
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

		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(webInvocationPrivilegeEvaluator, pluginType,
				molgenisUiMenu);
		assertEquals(xmlMolgenisUiPlugin.getName(), pluginName);
	}

	@Test
	public void getNameById()
	{
		String pluginId = "pluginId";
		PluginType pluginType = new PluginType();
		pluginType.setId(pluginId);

		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(webInvocationPrivilegeEvaluator, pluginType,
				molgenisUiMenu);
		assertEquals(xmlMolgenisUiPlugin.getName(), pluginId);
	}

	@Test
	public void getType()
	{
		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(webInvocationPrivilegeEvaluator,
				new PluginType(), molgenisUiMenu);
		assertEquals(xmlMolgenisUiPlugin.getType(), MolgenisUiMenuItemType.PLUGIN);
	}

	@Test
	public void getParentMenu()
	{
		assertEquals(
				new XmlMolgenisUiPlugin(webInvocationPrivilegeEvaluator, new PluginType(), molgenisUiMenu)
						.getParentMenu(),
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
		when(webInvocationPrivilegeEvaluator.isAllowed(MolgenisPlugin.PLUGIN_URI_PREFIX + id, authentication))
				.thenReturn(true);
		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(webInvocationPrivilegeEvaluator, pluginType,
				molgenisUiMenu);
		assertTrue(xmlMolgenisUiPlugin.isAuthorized());
	}

	@Test
	public void isAuthorized_notAuthorized()
	{
		PluginType pluginType = new PluginType();
		pluginType.setName("type_notauthorized");
		XmlMolgenisUiPlugin xmlMolgenisUiPlugin = new XmlMolgenisUiPlugin(webInvocationPrivilegeEvaluator,
				new PluginType(), molgenisUiMenu);
		assertFalse(xmlMolgenisUiPlugin.isAuthorized());
	}
}
