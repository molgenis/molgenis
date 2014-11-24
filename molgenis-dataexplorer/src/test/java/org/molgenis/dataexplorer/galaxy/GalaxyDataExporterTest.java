package org.molgenis.dataexplorer.galaxy;

import org.testng.annotations.Test;

public class GalaxyDataExporterTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void GalaxyDataExporter()
	{
		new GalaxyDataExporter(null, null);
	}

	// unit testing not possible, can't mock GalaxyInstanceFactory
}
