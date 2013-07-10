package org.molgenis.compute5.generators.local;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.generators.BackendGenerator;

public class LocalBackend extends BackendGenerator
{

	public LocalBackend(ComputeProperties cp) throws IOException
	{
		super(cp);
	}

}
