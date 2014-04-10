package org.molgenis.js;

import org.testng.annotations.BeforeMethod;

public class MolgenisJsTest
{

	@BeforeMethod
	public void beforeMethod()
	{
		new RhinoConfig().init();
	}

}
