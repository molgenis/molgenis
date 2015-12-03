package org.molgenis.framework.ui;

import static org.testng.Assert.assertEquals;

import org.molgenis.ui.MolgenisPluginController;
import org.testng.annotations.Test;

public class MolgenisPluginTest
{

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisPlugin()
	{
		new MolgenisPluginController("/invalidprefix/test")
		{
		};
	}

	@Test
	public void getId()
	{
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "test";
		MolgenisPluginController molgenisPlugin = new MolgenisPluginController(uri)
		{
		};
		assertEquals(molgenisPlugin.getId(), "test");
	}

	@Test
	public void getUri()
	{
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "test";
		MolgenisPluginController molgenisPlugin = new MolgenisPluginController(uri)
		{
		};
		assertEquals(molgenisPlugin.getUri(), uri);
	}
}
