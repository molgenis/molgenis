package org.molgenis.diseasematcher.data.preprocess;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

public class DiseaseMappingProcessorTest
{

	@Test
	public void testProcessFiles() throws FileNotFoundException, IOException
	{

		InputStream allIn = getClass().getResourceAsStream("/ALL_FREQUENCIES.txt");
		File allFile = new File(FileUtils.getTempDirectory(), "ALL_FREQUENCIES.txt");
		FileCopyUtils.copy(allIn, new FileOutputStream(allFile));

		InputStream typicalIn = getClass().getResourceAsStream("/TYPICAL_FEATURES.txt");
		File typicalFile = new File(FileUtils.getTempDirectory(), "TYPICAL_FEATURES.txt");
		FileCopyUtils.copy(typicalIn, new FileOutputStream(typicalFile));

		File outFile = File.createTempFile("diseasemapping", "tsv");

		DiseaseMappingProcessor dmp = new DiseaseMappingProcessor();
		dmp.processFiles(typicalFile, allFile, outFile);

		Reader in = new FileReader(outFile);
		String out = FileCopyUtils.copyToString(in);

		String check = "\"identifier\"	\"diseaseId\"	\"geneSymbol\"	\"geneId\"	\"HPOId\"	\"HPODescription\"	\"isTypical\"\n\"OMIM:303100CHMHP:0000505\"	\"OMIM:303100\"	\"CHM\"	\"1121\"	\"HP:0000505\"	\"Visual impairment\"	\"true\"\n\"ORPHANET:2318ZNF423HP:0002311\"	\"ORPHANET:2318\"	\"ZNF423\"	\"23090\"	\"HP:0002311\"	\"Incoordination\"	\"true\"\n\"ORPHANET:2318TMEM138HP:0002104\"	\"ORPHANET:2318\"	\"TMEM138\"	\"51524\"	\"HP:0002104\"	\"Apnea\"	\"false\"\n";

		assertEquals(out, check);
	}

}
