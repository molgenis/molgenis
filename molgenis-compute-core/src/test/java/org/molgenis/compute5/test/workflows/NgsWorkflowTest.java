package org.molgenis.compute5.test.workflows;

import java.io.IOException;

import org.molgenis.compute5.ComputeCommandLine;
import org.molgenis.compute5.model.Compute;
import org.testng.annotations.Test;

public class NgsWorkflowTest
{
	@Test
	public void test() throws IOException
	{
		Compute c = ComputeCommandLine.create("src/main/resources/workflows/ngs/workflow.csv", new String[]
				{ "src/main/resources/workflows/ngs/parameters.csv", "src/main/resources/workflows/ngs/settings.csv" }, "target/example-output/ngs");
	}
}
