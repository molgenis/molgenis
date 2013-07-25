package org.molgenis.compute5;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 7/16/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class FoldingTest
{
	private String outputDir = "target/test/benchmark/run";

	@Test
	public void testFolding1() throws Exception
	{
		System.out.println("--- Start Test Folding 1---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate",
				"--workflow",
				"src/main/resources/workflows/testfolding/workflow.csv",
				"--parameters",
				"src/main/resources/workflows/testfolding/parameters.csv",
				"--rundir",
				outputDir
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/test1_0.sh");
		if (!file.exists())
		{
			Assert.fail("test1_0.sh is not generated");
		}

		file = new File(outputDir + "/test1_4.sh");
		if (!file.exists())
		{
			Assert.fail("test1_5.sh is not generated");
		}

		file = new File(outputDir + "/test2_1.sh");
		if (!file.exists())
		{
			Assert.fail("test2_1.sh is not generated");
		}

		file = new File(outputDir + "/test2_2.sh");
		if (file.exists())
		{
			Assert.fail("test2_2.sh should not be generated");
		}

		file = new File(outputDir + "/test3_2.sh");
		if (!file.exists())
		{
			Assert.fail("test3_2.sh is not generated");
		}

		file = new File(outputDir + "/test3_3.sh");
		if (file.exists())
		{
			Assert.fail("test3_3.sh should not be generated");
		}

		file = new File(outputDir + "/test4_1.sh");
		if (!file.exists())
		{
			Assert.fail("test4_1.sh is not generated");
		}

		file = new File(outputDir + "/test4_2.sh");
		if (file.exists())
		{
			Assert.fail("test4_2.sh should not be generated");
		}

		file = new File(outputDir + "/test5_1.sh");
		if (!file.exists())
		{
			Assert.fail("test5_1.sh is not generated");
		}

		file = new File(outputDir + "/test5_2.sh");
		if (file.exists())
		{
			Assert.fail("test5_2.sh should not be generated");
		}


		System.out.println("--- Test Lists Correctness ---");

		//this conditions can be change later, when compute will weave parameters directly

		String test2_0_list1 = "chunk[0]=${chunk[0]}\n" +
				"chunk[1]=${chunk[1]}\n" +
				"chunk[2]=${chunk[2]}\n";
		String test2_0_list2 ="chr[0]=${chr[0]}\n" +
				"chr[1]=${chr[1]}\n" +
				"chr[2]=${chr[2]}";

		String test2_1_list1 = "chunk[0]=${chunk[3]}\n" +
				"chunk[1]=${chunk[4]}\n";
		String test2_1_list2 ="chr[0]=${chr[3]}\n" +
				"chr[1]=${chr[4]}";

		String test3_0_list1 = "chunk[0]=${chunk[0]}\n" +
				"chunk[1]=${chunk[3]}\n";
		String test3_0_list2 ="chr[0]=${chr[0]}\n" +
				"chr[1]=${chr[3]}";

		String test3_1_list1 = "chunk[0]=${chunk[1]}\n" +
				"chunk[1]=${chunk[4]}\n";
		String test3_1_list2 = "chr[0]=${chr[1]}\n" +
				"chr[1]=${chr[4]}";

		String test3_2_list1 = "chunk[0]=${chunk[2]}\n";
		String test3_2_list2 =	"chr[0]=${chr[2]}";

		String test_weaving_2_0 = "for s in \"a\" \"b\" \"c\"";
		String test_weaving_2_1 = "for s in \"a\" \"b\"";

		String t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_0.sh");

		if(!t.contains(test2_0_list1) || !t.contains(test2_0_list2))
		{
			Assert.fail("folding broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_1.sh");
		if(!t.contains(test2_1_list1) || !t.contains(test2_1_list2))
		{
			Assert.fail("folding broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test3_0.sh");
		if(!t.contains(test3_0_list1) || !t.contains(test3_0_list2))
		{
			Assert.fail("folding broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test3_1.sh");

		if(!t.contains(test3_1_list1) || !t.contains(test3_1_list2))
		{
			Assert.fail("folding broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test3_2.sh");
		if(!t.contains(test3_2_list1) || !t.contains(test3_2_list2))
		{
			Assert.fail("folding broken");
		}

		System.out.println("Test Weaving Correctness");

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_0.sh");
		if(!t.contains(test_weaving_2_0))
		{
			Assert.fail("weaving is broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_1.sh");
		if(!t.contains(test_weaving_2_1))
		{
			Assert.fail("weaving is broken");
		}


	}

}
