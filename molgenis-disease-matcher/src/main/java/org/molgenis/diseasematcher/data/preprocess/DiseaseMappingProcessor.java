package org.molgenis.diseasematcher.data.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.poi.util.IOUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.MapEntity;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Command-line tool for combining two disease/gene/hpo mapping files (TYPICAL_FEATURES and ALL_FREQUENCIES) and
 * processing them for use as a DiseaseMapping in Molgenis.
 * 
 * Files can be downloaded from:
 * http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastStableBuild/artifact/annotation/ Choose the
 * ALL_SOURCES, OMIM or ORPHANET diseases_to_genes_to_phenotypes.txt files.
 * 
 * @author tommydeboer
 */
public class DiseaseMappingProcessor
{
	/**
	 * Entry point of tool.
	 * 
	 * @param args
	 *            TYPICAL_FEATURES and ALL_FREQUENCIES files
	 */
	public static void main(String[] args)
	{
		DiseaseMappingProcessor dmp = new DiseaseMappingProcessor();

		try
		{
			File typicalFile = null;
			File allFile = null;

			// fetch arguments
			if (args.length != 2)
			{
				dmp.printHelp();
				System.exit(0);
			}
			else
			{
				typicalFile = new File(args[0]);
				allFile = new File(args[1]);
			}

			// check if files exist
			if (!typicalFile.isFile())
			{
				throw new FileNotFoundException(typicalFile.getAbsolutePath());
			}
			if (!allFile.isFile())
			{
				throw new FileNotFoundException(allFile.getAbsolutePath());
			}

			File outFile = new File("disease-gene-phenotype.tsv");
			System.out.println("Writing combined file to: " + outFile.getAbsolutePath());

			dmp.processFiles(typicalFile, allFile, outFile);

		}
		catch (FileNotFoundException e)
		{
			System.err.println("Could not find file: " + e.getMessage());
			dmp.printHelp();
		}

	}

	/**
	 * Combines a TYPICAL_FEATURES file with a ALL_FREQUENCIES file in the DiseaseMapping format and writes it to the
	 * specified output file. Make sure the two files are of the same source (OMIM, ORPHANET or ALL).
	 * 
	 * @param typicalFile
	 *            a TYPICAL_FEATURES mapping file
	 * @param allFile
	 *            an ALL_FREQUENCIES mapping file
	 * @param outFile
	 *            the combined pre-processed output file
	 */
	public void processFiles(File typicalFile, File allFile, File outFile)
	{
		CsvWriter csvWriter = null;
		CSVReader csvReaderTypical = null;
		CSVReader csvReaderAll = null;

		try
		{
			// instantiate readers and writers
			csvReaderTypical = new CSVReader(new BufferedReader(new FileReader(typicalFile)), '\t');
			csvReaderAll = new CSVReader(new BufferedReader(new FileReader(allFile)), '\t');

			csvWriter = new CsvWriter(new BufferedWriter(new FileWriter(outFile)), '\t');

			// write header
			csvWriter.writeAttributeNames(Arrays.asList("identifier", "diseaseId", "geneSymbol", "geneId", "HPOId",
					"HPODescription", "isTypical"));

			// read the typical file and store all entries in a map for later reference
			HashMap<String, String[]> typicalMap = new HashMap<String, String[]>();
			String[] line = csvReaderTypical.readNext(); // skip header
			line = csvReaderTypical.readNext();
			while (line != null)
			{
				// combine diseaseId, geneSymbol and HPOId to get unique id
				String id = line[0] + line[1] + line[3];
				typicalMap.put(id, line);

				line = csvReaderTypical.readNext();
			}

			// read the ALL file and write to output, appending true if an entry is also present in the typical file
			line = csvReaderAll.readNext(); // skip header
			line = csvReaderAll.readNext();
			while (line != null)
			{
				String id = line[0] + line[1] + line[3];
				Entity entity = new MapEntity();
				entity.set("identifier", id);
				entity.set("diseaseId", line[0]);
				entity.set("geneSymbol", line[1]);
				entity.set("geneId", line[2]);
				entity.set("HPOId", line[3]);
				entity.set("HPODescription", line[4]);

				if (typicalMap.containsKey(id))
				{
					entity.set("isTypical", true);
				}
				else
				{
					entity.set("isTypical", false);
				}
				csvWriter.add(entity);

				line = csvReaderAll.readNext();
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			IOUtils.closeQuietly(csvReaderTypical);
			IOUtils.closeQuietly(csvReaderAll);
			IOUtils.closeQuietly(csvWriter);
		}

	}

	/**
	 * Prints instructions for the user.
	 */
	private void printHelp()
	{
		System.out
				.println("Combines the TYPICAL_FEATURES and ALL_FREQUENCIES disease/gene/hpo mapping files and prepares them for importing in Molgenis");
		System.out.println("Files can be downloaded from:");
		System.out
				.println("http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastStableBuild/artifact/annotation/");
		System.out.println("Usage:");
		System.out.println("OmimGeneHpoProcessor.java <TYPICAL_FEATURES_file> <ALL_FREQUENCIES_file>");
	}
}
