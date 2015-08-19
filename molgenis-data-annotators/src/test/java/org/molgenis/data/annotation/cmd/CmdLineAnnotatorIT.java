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
public class CmdLineAnnotatorIT
{
	@Test
	public void gonl() throws Exception
	{
		testAnnotator("gonl", "test.vcf", "test-out-expected.vcf");
	}

	private void testAnnotator(String name, String inputFileName, String expectedOutputFileName) throws Exception
	{
		String resourceDir = "src/test/resources/" + name;
		String inputFile = resourceDir + "/" + inputFileName;
		String outputFile = "target/out-" + inputFileName;
		String expectedOutputFile = resourceDir + "/" + expectedOutputFileName;

		CmdLineAnnotator.main(new String[]
		{ name, resourceDir, inputFile, outputFile });

		assertEquals(readLines(expectedOutputFile), readLines(outputFile));
	}

	private List<String> readLines(String file) throws IOException
	{
		return FileUtils.readLines(new File(file));
	}
}
