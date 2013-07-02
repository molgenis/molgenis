package org.molgenis.compute5;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.TestNG;

import org.testng.annotations.Test;

public class ComputeCommandLineTest
{
	@Test
	public void testHelp1()
	{
		try
		{
			ComputeCommandLine.main(new String[]{"-h"});
		}
		catch (Exception e)
		{
			Assert.fail("compute -h does not work");
		}

	}

	@Test
	public void testCommandLineParametersVSComputeProperties() throws ParseException, IOException, ClassNotFoundException
	{
		System.out.println("--- Start TestCommandLineParametersVSComputeProperties ---");

		ComputeCommandLine.main(new String[]{
				"--generate", "--run", "--workflow", "src/main/resources/workflows/benchmark/workflow.csv",
						"--defaults", "src/main/resources/workflows/benchmark/workflow.defaults.csv",
						"--parameters","src/main/resources/workflows/benchmark/parameters.csv",
						"--rundir","src/main/resources/workflows/benchmark/run",
						"--backend","pbs",
						"--database","none"});


		String resultProperties =  getFileAsString(".compute.properties");

		if(!resultProperties.contains("rundir=src/main/resources/workflows/benchmark/run"))
		{
			Assert.fail("rundir parameter is failed");
		}

		if(!resultProperties.contains("defaults=./src/main/resources/workflows/benchmark/workflow.defaults.csv"))
		{
			Assert.fail("defaults parameter is failed");
		}

		if(!resultProperties.contains("workflow=./src/main/resources/workflows/benchmark/workflow.csv"))
		{
			Assert.fail("workflow parameter is failed");
		}

		if(!resultProperties.contains("parameters=./src/main/resources/workflows/benchmark/parameters.csv"))
		{
			Assert.fail("parameters parameter is failed");
		}

		if(!resultProperties.contains("backend=pbs"))
		{
			Assert.fail("backend parameter is failed");
		}

	}

	private final String getFileAsString(String filename) throws IOException
	{
		File file = new File(filename);

		if (!file.exists())
		{
			Assert.fail(".compute.properties file does not exist");
		}
		final BufferedInputStream bis = new BufferedInputStream(
				new FileInputStream(file));
		final byte[] bytes = new byte[(int) file.length()];
		bis.read(bytes);
		bis.close();
		return new String(bytes);
	}

}
