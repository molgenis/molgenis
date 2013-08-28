package org.molgenis;

import static org.testng.Assert.assertEquals;

import org.molgenis.framework.ui.MolgenisPlugin;
import org.testng.annotations.Test;

public class MolgenisPluginTest
{

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisPlugin()
	{
		new MolgenisPlugin("/invalidprefix/test")
		{
		};
	}

	@Test
	public void getId()
	{
		String uri = MolgenisPlugin.PLUGIN_URI_PREFIX + "test";
		MolgenisPlugin molgenisPlugin = new MolgenisPlugin(uri)
		{
		};
		assertEquals(molgenisPlugin.getId(), "test");
	}

	@Test
	public void getUri()
	{
		String uri = MolgenisPlugin.PLUGIN_URI_PREFIX + "test";
		MolgenisPlugin molgenisPlugin = new MolgenisPlugin(uri)
		{
		};
		assertEquals(molgenisPlugin.getUri(), uri);
	}
}
