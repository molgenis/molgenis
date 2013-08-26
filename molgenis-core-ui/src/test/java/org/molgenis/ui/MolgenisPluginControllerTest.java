package org.molgenis.ui;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class MolgenisPluginControllerTest
{

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisPluginController()
	{
		new MolgenisPluginController("/invalidprefix/test")
		{
		};
	}

	@Test
	public void getId()
	{
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "test";
		MolgenisPluginController molgenisPluginController = new MolgenisPluginController(uri)
		{
		};
		assertEquals(molgenisPluginController.getId(), "test");
	}

	@Test
	public void getUri()
	{
		String uri = MolgenisPluginController.PLUGIN_URI_PREFIX + "test";
		MolgenisPluginController molgenisPluginController = new MolgenisPluginController(uri)
		{
		};
		assertEquals(molgenisPluginController.getUri(), uri);
	}
}
