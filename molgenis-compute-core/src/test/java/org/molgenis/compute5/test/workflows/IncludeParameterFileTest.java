package org.molgenis.compute5.test.workflows;

import java.io.IOException;

import org.molgenis.compute5.ComputeCommandLine;
import org.molgenis.compute5.model.Compute;
import org.testng.annotations.Test;

public class IncludeParameterFileTest
{
	@Test
	public void test() throws IOException
	{
		Compute c = ComputeCommandLine.create("", new String[]
		{ "src/main/resources/workflows/example1.csv", "src/main/resources/workflows/includeParams/parameters.csv" },
				"target/example-output/includeParameters");
	}
}
