package org.molgenis.annotation.test.cmd.integration;

import org.apache.commons.io.FileUtils;
import org.molgenis.annotation.cmd.CmdLineAnnotator;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Annotator integration tests
 * <p>
 * Tip: if you only want to run the integration tests in maven use: mvn failsafe:integration-test
 */
public class EdgeCaseExacCmdLineAnnotatorIT
{
	@Test
	public void exac() throws Exception
	{
		testAnnotator("exac", ResourceUtils.getFile(getClass(), "/exac/exacIT_set.vcf.gz").getPath(),
				"test-edgecases.vcf", "test-edgecases-out-expected.vcf");
	}

	private void testAnnotator(String name, String resourceLocation, String inputFileName,
			String expectedOutputFileName) throws Exception
	{
		String resourceDir = ResourceUtils.getFile(getClass(), "/").getPath() + File.separator + name;
		String inputFile = resourceDir + "/" + inputFileName;
		String outputFile = resourceDir + "/out-" + name + "-" + inputFileName;
		String expectedOutputFile = resourceDir + "/" + expectedOutputFileName;

		CmdLineAnnotator
				.main(new String[] { "-a", name, "-s", resourceLocation, "-i", inputFile, "-o", outputFile, "-r" });

		assertEquals(readLines(outputFile), readLines(expectedOutputFile));
	}

	private List<String> readLines(String file) throws IOException
	{
		return FileUtils.readLines(new File(file));
	}
}
