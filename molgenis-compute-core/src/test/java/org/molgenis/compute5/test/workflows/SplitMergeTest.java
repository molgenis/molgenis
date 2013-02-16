package org.molgenis.compute5.test.workflows;

import java.io.IOException;

import org.molgenis.compute5.ComputeCommandLine;
import org.molgenis.compute5.model.Compute;
import org.testng.annotations.Test;

public class SplitMergeTest
{
	@Test
	public void test() throws IOException
	{
		Compute c = ComputeCommandLine.create("workflows/splitmerge/workflow.csv", new String[]
		{ "workflows/splitmerge/parameters.csv" }, "example-output/splitmerge");

	}
}
