package org.molgenis.ui;

import org.testng.annotations.Test;

public class MolgenisMenuControllerTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisMenuController()
	{
		new MolgenisMenuController(null);
	}

	@Test
	public void forwardDefaultMenuDefaultPlugin()
	{
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void forwardMenuDefaultPlugin()
	{
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void forwardMenuPluginStringStringModel()
	{
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void forwardMenuPluginStringStringStringModel()
	{
		throw new RuntimeException("Test not implemented");
	}

	@Test
	public void getForwardPluginUri()
	{
		throw new RuntimeException("Test not implemented");
	}
}
