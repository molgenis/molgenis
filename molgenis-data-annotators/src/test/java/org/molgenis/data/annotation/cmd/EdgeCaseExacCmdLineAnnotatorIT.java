package org.molgenis.data.annotation.cmd;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

/**
 * Annotator integration tests
 * 
 * Tip: if you only want to run the integration tests in maven use: mvn failsafe:integration-test
 */
public class EdgeCaseExacCmdLineAnnotatorIT
{
	@Test
	public void exac() throws Exception
	{
		testAnnotator("exac", "src/test/resources/exac/exacIT_set.vcf.gz", "test-edgecases.vcf", "test-edgecases-out-expected.vcf");
	}

	private void testAnnotator(String name, String resourceLocation, String inputFileName, String expectedOutputFileName)
			throws Exception
	{
		String resourceDir = "src/test/resources/" + name;
		String inputFile = resourceDir + "/" + inputFileName;
		String outputFile = "target/out-" + name + "-" + inputFileName;
		String expectedOutputFile = resourceDir + "/" + expectedOutputFileName;

		CmdLineAnnotator.main(new String[]
		{ "-a", name, "-s", resourceLocation, "-i", inputFile, "-o", outputFile, "-r"});

		assertEquals(readLines(outputFile), readLines(expectedOutputFile));
	}

	private List<String> readLines(String file) throws IOException
	{
		return FileUtils.readLines(new File(file));
	}
}
