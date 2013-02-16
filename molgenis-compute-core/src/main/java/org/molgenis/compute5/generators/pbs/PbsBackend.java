package org.molgenis.compute5.generators.pbs;

import java.io.IOException;

import org.molgenis.compute5.generators.BackendGenerator;

public class PbsBackend extends BackendGenerator
{
	public PbsBackend() throws IOException
	{
		super("header.ftl","footer.ftl","submit.ftl");
	}
}
