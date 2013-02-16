package org.molgenis.compute5.generators.local;

import java.io.IOException;

import org.molgenis.compute5.generators.BackendGenerator;

public class LocalBackend extends BackendGenerator
{

	public LocalBackend() throws IOException
	{
		super("header.ftl", "footer.ftl", "submit.ftl");
	}

}
