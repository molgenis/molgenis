package org.molgenis.compute5.test.workflows;

import java.io.IOException;

import org.molgenis.compute5.ComputeCommandLine;
import org.molgenis.compute5.model.Compute;
import org.testng.annotations.Test;

public class DemoTest
{
	@Test
	public void test() throws IOException
	{
		Compute c = ComputeCommandLine.create("demo/workflow.csv", new String[]
		{ "demo/parameters.csv" }, "demo");
	}
}
