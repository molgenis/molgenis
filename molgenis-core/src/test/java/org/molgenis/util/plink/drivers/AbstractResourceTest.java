package org.molgenis.util.plink.drivers;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class AbstractResourceTest
{
	protected File getTestResource(String name) throws URISyntaxException
	{
		URI resource = this.getClass().getResource(name).toURI();
		return new File(resource);
	}
}
