package org.molgenis.data.annotation.cmd;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
		testAnnotator("gonl", "src/test/resources/gonl", "test.vcf", "test-out-expected.vcf");
	}

	@Test
	public void hpo() throws Exception
	{
		testAnnotator("hpo", "src"+File.separator+"test"+File.separator+"resources"+File.separator+"hpo"+File.separator+"hpo.txt", "test.vcf", "test-out-expected.vcf");
	}

	@Test
	public void hpoTermsOnly() throws Exception
	{
		testAnnotator("hpo", "src"+File.separator+"test"+File.separator+"resources"+File.separator+"hpo"+File.separator+"hpo.txt", "test.vcf", "test-out-terms-expected.vcf",
				Arrays.asList("HPOTERMS"));
	}

	@Test
	public void fitcon() throws Exception
	{
		testAnnotator("fitcon", "src/test/resources/fitcon/fitcon_test_set.tsv.gz", "test.vcf", "test-out-expected.vcf");
	}

	private void testAnnotator(String name, String resourceLocation, String inputFileName, String expectedOutputFileName)
			throws Exception
	{
		testAnnotator(name, resourceLocation, inputFileName, expectedOutputFileName, Collections.emptyList());
	}

	private void testAnnotator(String name, String resourceLocation, String inputFileName,
			String expectedOutputFileName, List<String> attributesToInclude) throws Exception
	{
		String resourceDir = "src/test/resources/" + name;
		String inputFile = resourceDir + "/" + inputFileName;
		String outputFile = "target/out-" + name + "-" + inputFileName;
		String expectedOutputFile = resourceDir + "/" + expectedOutputFileName;

		List<String> args = new ArrayList<>(Arrays.asList("-a", name, "-s", resourceLocation, "-i", inputFile, "-o",
				outputFile));
		args.addAll(attributesToInclude);
		CmdLineAnnotator.main(args.toArray(new String[args.size()]));

		try
		{
			assertEquals(readLines(outputFile), readLines(expectedOutputFile));
		}
		finally
		{
			File output = new File(outputFile);
			if (output.exists()) output.delete();
		}
	}

	private List<String> readLines(String file) throws IOException
	{
		return FileUtils.readLines(new File(file));
	}
}
