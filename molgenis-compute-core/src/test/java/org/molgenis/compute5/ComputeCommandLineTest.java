package org.molgenis.compute5;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.testng.annotations.Test;

public class ComputeCommandLineTest
{
	@Test
	public void test1() throws ComputeException, ParseException, ClassNotFoundException, IOException
	{
		ComputeCommandLine.main(new String[]{"-h"});
	}
	
	@Test
	public void test2() throws ComputeException, ParseException, ClassNotFoundException, IOException
	{
		String path = "target/test1";
		this.runtest(path, new String[]{"--path","src/main/resources/workflows/demoNBIC2","--rundir",path, "-g", "--workflow", "parameters/workflow.csv"});
	}
	
	public void runtest(String dir, String[] params) throws IOException, ParseException, ClassNotFoundException
	{
		File f = new File(dir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());
		ComputeCommandLine.main(params);
		Assert.assertTrue(f.exists());
	}
}
