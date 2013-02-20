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
		Compute c = ComputeCommandLine.create("src/main/resources/workflows/copyFile/workflow.csv", new String[]
		{ "src/main/resources/workflows/copyFile/parameters.csv", "src/main/resources/workflows/copyFile/constants.csv" }, "example-output/copyfile");
	}
}
