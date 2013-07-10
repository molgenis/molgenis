package org.molgenis.compute5.generators.pbs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.generators.BackendGenerator;

public class PbsBackend extends BackendGenerator
{

	public PbsBackend(ComputeProperties cp) throws IOException
	{
		super(cp);
	}

}
