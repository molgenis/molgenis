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
	public void testHelp()
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
	public void testClear()
	{
		try
		{
			ComputeCommandLine.main(new String[]{"--clear"});

			File f = new File(".compute.properties");

			if(f.exists())
			{
				Assert.fail(".compute.properties is not deleted");
			}

		}
		catch (Exception e)
		{
			Assert.fail("compute --clear does not work");
		}
	}

	@Test
	public void testCommandLineParametersComputePropertiesFilesCreated() throws ParseException, IOException, ClassNotFoundException
	{
		System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

		File f = new File("src/main/resources/workflows/benchmark/run");
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate", "--run", "--workflow", "src/main/resources/workflows/benchmark/workflow.csv",
						"--defaults", "src/main/resources/workflows/benchmark/workflow.defaults.csv",
						"--parameters","src/main/resources/workflows/benchmark/parameters.csv",
						"--rundir","src/main/resources/workflows/benchmark/run",
						"--backend","pbs",
						"--database","none",
						"-header", "src/main/resources/workflows/benchmark/header.ftl",
						"-footer", "src/main/resources/workflows/benchmark/footer.ftl"
						});


		System.out.println("--- Test Compute Properties ---");
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

		System.out.println("--- Test Created Files ---");

		File file = new File("src/main/resources/workflows/benchmark/run/step1_0.sh");
		if (!file.exists())
		{
			Assert.fail("step1_0.sh is not generated");
		}

		file = new File("src/main/resources/workflows/benchmark/run/step1_1.sh");
		if (!file.exists())
		{
			Assert.fail("step1_1.sh is not generated");
		}

		file = new File("src/main/resources/workflows/benchmark/run/step2_0.sh");
		if (!file.exists())
		{
			Assert.fail("step2_0.sh is not generated");
		}

		file = new File("src/main/resources/workflows/benchmark/run/submit.sh");
		if (!file.exists())
		{
			Assert.fail("submit.sh is not generated");
		}

		file = new File("src/main/resources/workflows/benchmark/run/user.env");
		if (!file.exists())
		{
			Assert.fail("user.env is not generated");
		}

		System.out.println("--- Test correct headers insertion ---");

		String script = getFileAsString("src/main/resources/workflows/benchmark/run/step1_0.sh");

		if(!script.contains("# My own custom header"))
		{
			Assert.fail("header is not correctly inserted");
		}

		if(!script.contains("# My own custom footer"))
		{
			Assert.fail("footer is not correctly inserted");
		}

		System.out.println("--- Test correct data management ---");

		if(!script.contains("getFile()") || !script.contains("putFile()"))
		{
			Assert.fail("get/put file is not inserted");
		}
	}

	@Test
	public void testPathparameter() throws ParseException, IOException, ClassNotFoundException
	{
		System.out.println("--- Testing path parameter ---");

		File f = new File("src/main/resources/workflows/benchmark/run");
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate", "--run", "--path", "src/main/resources/workflows/benchmark/",
				"--workflow", "workflow.csv",
				"--defaults", "workflow.defaults.csv",
				"--parameters","parameters.csv",
				"--rundir","src/main/resources/workflows/benchmark/run",
				"--backend","pbs",
				"--database","none"});


		System.out.println("--- Test Compute Properties ---");
		String resultProperties =  getFileAsString(".compute.properties");

		if(!resultProperties.contains("rundir=src/main/resources/workflows/benchmark/run"))
		{
			Assert.fail("rundir parameter is failed");
		}

		if(!resultProperties.contains("defaults=src/main/resources/workflows/benchmark/workflow.defaults.csv"))
		{
			Assert.fail("defaults parameter is failed");
		}

		if(!resultProperties.contains("workflow=src/main/resources/workflows/benchmark/workflow.csv"))
		{
			Assert.fail("workflow parameter is failed");
		}

		if(!resultProperties.contains("parameters=src/main/resources/workflows/benchmark/parameters.csv"))
		{
			Assert.fail("parameters parameter is failed");
		}

		if(!resultProperties.contains("backend=pbs"))
		{
			Assert.fail("backend parameter is failed");
		}

		System.out.println("--- Test Created Files ---");

		File file = new File("src/main/resources/workflows/benchmark/run/step1_0.sh");
		if (!file.exists())
		{
			Assert.fail("step1_0.sh is not generated");
		}

		file = new File("src/main/resources/workflows/benchmark/run/step1_1.sh");
		if (!file.exists())
		{
			Assert.fail("step1_1.sh is not generated");
		}

		file = new File("src/main/resources/workflows/benchmark/run/step2_0.sh");
		if (!file.exists())
		{
			Assert.fail("step2_0.sh is not generated");
		}

		file = new File("src/main/resources/workflows/benchmark/run/submit.sh");
		if (!file.exists())
		{
			Assert.fail("submit.sh is not generated");
		}

		file = new File("src/main/resources/workflows/benchmark/run/user.env");
		if (!file.exists())
		{
			Assert.fail("user.env is not generated");
		}

		System.out.println("--- Test correct headers insertion ---");

		String script = getFileAsString("src/main/resources/workflows/benchmark/run/step1_0.sh");

		if(!script.contains("# My own custom header"))
		{
			Assert.fail("header is not correctly inserted");
		}

		if(!script.contains("# My own custom footer"))
		{
			Assert.fail("footer is not correctly inserted");
		}

		System.out.println("--- Test correct data management ---");

		if(!script.contains("getFile()") || !script.contains("putFile()"))
		{
			Assert.fail("get/put file is not inserted");
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
