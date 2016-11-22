package org.molgenis.annotation.test.cmd.integration;

import org.apache.commons.io.FileUtils;
import org.molgenis.annotation.cmd.CmdLineAnnotator;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Annotator integration tests
 * <p>
 * Tip: if you only want to run the integration tests in maven use: mvn failsafe:integration-test
 */
public class CmdLineAnnotatorIT
{
	@Test
	public void gonl() throws Exception
	{
		testAnnotator("gonl", ResourceUtils.getFile(getClass(), "/gonl").getPath(), "test.vcf",
				"test-out-expected.vcf");
	}

	@Test
	public void hpo() throws Exception
	{
		testAnnotator("hpo", ResourceUtils.getFile(getClass(), "/hpo/hpo.txt").getPath(), "test.vcf",
				"test-out-expected.vcf");
	}

	@Test
	public void hpoTermsOnly() throws Exception
	{
		testAnnotator("hpo", ResourceUtils.getFile(getClass(), "/hpo/hpo.txt").getPath(), "test.vcf",
				"test-out-terms-expected.vcf", Arrays.asList("HPOTERMS"));
	}

	@Test
	public void fitcon() throws Exception
	{
		testAnnotator("fitcon", ResourceUtils.getFile(getClass(), "/fitcon/fitcon_test_set.tsv.gz").getPath(),
				"test.vcf", "test-out-expected.vcf");
	}

	private void testAnnotator(String name, String resourceLocation, String inputFileName,
			String expectedOutputFileName) throws Exception
	{
		testAnnotator(name, resourceLocation, inputFileName, expectedOutputFileName, Collections.emptyList());
	}

	private void testAnnotator(String name, String resourceLocation, String inputFileName,
			String expectedOutputFileName, List<String> attributesToInclude) throws Exception
	{
		String resourceDir = ResourceUtils.getFile(getClass(), "/").getPath() + File.separator + name;
		String inputFile = resourceDir + "/" + inputFileName;
		String outputFile = resourceDir + "/out-" + name + "-" + inputFileName;
		String expectedOutputFile = resourceDir + "/" + expectedOutputFileName;

		List<String> args = new ArrayList<>(
				Arrays.asList("-a", name, "-s", resourceLocation, "-i", inputFile, "-o", outputFile));
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
