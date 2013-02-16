package org.molgenis.compute5.test.workflows;

import java.io.IOException;

import org.molgenis.compute5.ComputeCommandLine;
import org.molgenis.compute5.model.Compute;
import org.testng.annotations.Test;

public class CopyFileWorkflowTest
{
	@Test
	public void test() throws IOException
	{
		Compute c = ComputeCommandLine.create("workflows/copyFile/workflow.csv", new String[]
		{ "workflows/copyFile/parameters.csv", "workflows/copyFile/constants.csv" }, "example-output/copyfile");
	}
}
