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
		{ "workflows/example.csv", "workflows/includeParams/main.csv"}, "example-output/includeParameters");
	}
}
