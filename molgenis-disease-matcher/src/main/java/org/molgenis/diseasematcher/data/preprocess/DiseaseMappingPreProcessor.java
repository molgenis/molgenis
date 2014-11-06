package org.molgenis.diseasematcher.data.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.poi.util.IOUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.ZipUtils;
import org.molgenis.util.ZipUtils.DirectoryStructure;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Command-line tool for preparing a DiseaseMapping and a DiseaseNames dataset for uploading to Molgenis. Combines two
 * disease/gene/hpo mapping files (TYPICAL_FEATURES and ALL_FREQUENCIES) and processes them as a DiseaseMapping. Also
 * takes the OMIM morbidmap file and processes disease name and identifier couples to DiseaseNames.tsv.
 * 
 * Files can be downloaded from:
 * http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastStableBuild/artifact/annotation/
 * http://www.omim.org/downloads
 * 
 * @author tommydeboer
 */
public class DiseaseMappingPreProcessor
{
	private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

	private static final String DISEASEMAPPING_FILENAME = "DiseaseMapping.tsv";
	private static final String DISEASE_FILENAME = "Disease.tsv";
	private static final String ZIP_FILENAME = "DiseaseMapping.zip";

	/**
	 * Main method of tool.
	 * 
	 * @param args
	 *            command-line arguments
	 */
	public static void main(String[] args)
	{
		DiseaseMappingPreProcessor dmpp = new DiseaseMappingPreProcessor();

		Options helpOptions = dmpp.createHelpOptions();
		Options toolOptions = dmpp.createToolOptions();

		CommandLineParser parser = new BasicParser();
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(100);

		File diseaseMappingOutFile = null;
		File diseaseNamesOutFile = null;

		try
		{
			// See if help option flag is set
			CommandLine line = parser.parse(helpOptions, args, true);
			if (line.hasOption("help"))
			{
				formatter.printHelp("DiseaseMappingPreProcessor", toolOptions, true);
			}
			else
			{
				// Parse the commandline arguments
				line = parser.parse(toolOptions, args);
				File typicalFile = new File(line.getOptionValue("typical"));
				File allFile = new File(line.getOptionValue("all"));
				File morbidmapFile = new File(line.getOptionValue("morbidmap"));
				File outDir = new File(line.getOptionValue("out"));

				// check if files/directories exist
				if (!typicalFile.isFile())
				{
					throw new FileNotFoundException("Could not find " + typicalFile.getAbsolutePath());
				}
				if (!allFile.isFile())
				{
					throw new FileNotFoundException("Could not find " + allFile.getAbsolutePath());
				}
				if (!morbidmapFile.isFile())
				{
					throw new FileNotFoundException("Could not find " + morbidmapFile);
				}
				if (!outDir.isDirectory())
				{
					throw new NotDirectoryException(outDir + " is not a valid directory");
				}

				// process files
				System.out.println("Combining TYPICAL_FEATURES and ALL_FREQUENCIES files to " + DISEASEMAPPING_FILENAME
						+ " ...");
				diseaseMappingOutFile = new File(outDir.getAbsolutePath() + "/" + DISEASEMAPPING_FILENAME);
				dmpp.makeDiseaseMappingFile(typicalFile, allFile, diseaseMappingOutFile);

				System.out.println("Parsing morbidmap to " + DISEASE_FILENAME + " ...");
				diseaseNamesOutFile = new File(outDir.getAbsolutePath() + "/" + DISEASE_FILENAME);
				dmpp.makeDiseaseNamesFile(morbidmapFile, diseaseNamesOutFile);

				// zip output files
				System.out.println("Zipping files to " + ZIP_FILENAME + " in " + outDir.getAbsolutePath());
				List<File> files = new ArrayList<File>();
				files.add(diseaseNamesOutFile);
				files.add(diseaseMappingOutFile);

				File outputZipFile = new File(outDir.getAbsolutePath(), ZIP_FILENAME);
				ZipUtils.compress(files, outputZipFile, DirectoryStructure.EXCLUDE_DIR);
			}
		}
		catch (ParseException exp)
		{
			System.err.println(exp.getMessage());
			formatter.printHelp("DiseaseMappingPreProcessor", toolOptions, true);
		}
		catch (FileNotFoundException e)
		{
			System.err.println(e.getMessage());
			formatter.printHelp("DiseaseMappingPreProcessor", toolOptions, true);
		}
		catch (NotDirectoryException e)
		{
			System.err.println(e.getMessage());
			formatter.printHelp("DiseaseMappingPreProcessor", toolOptions, true);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			// remove temp files
			System.out.println("Removing temporary files...");
			diseaseMappingOutFile.delete();
			diseaseNamesOutFile.delete();
		}
	}

	/**
	 * Takes the morbidmap from OMIM and turns it in to DiseaseNames.tsv. Only stores the disease name, OMIM identifier
	 * and mapping method fields.
	 * 
	 * @param morbidmapFile
	 *            the OMIM morbidmap file
	 * @param outFile
	 *            the output file
	 */
	public void makeDiseaseNamesFile(File morbidmapFile, File outFile)
	{
		CsvWriter csvWriter = null;
		CSVReader csvReader = null;

		try
		{
			csvReader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(morbidmapFile),
					CHARSET_UTF8)), '|');
			csvWriter = new CsvWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile),
					CHARSET_UTF8)), '\t');

			// write header
			csvWriter.writeAttributeNames(Arrays.asList("identifier", "diseaseId", "diseaseName", "mappingMethod"));

			HashSet<String> ids = new HashSet<String>();
			String line[] = csvReader.readNext();
			while (line != null)
			{
				// last three chars is always the mapping method
				String entry = line[0];
				String identifier = entry;

				// ignore double entries
				if (ids.contains(identifier))
				{
					line = csvReader.readNext();
					continue;
				}
				ids.add(identifier);

				String mappingMethod = entry.substring(entry.length() - 3);

				entry = entry.substring(0, entry.length() - 3);
				entry = entry.trim();

				String omimId = entry.substring(entry.length() - 6);

				// ignore entries without OMIM id
				if (omimId.matches("[0-9]+"))
				{
					// separate omimName form entry string and remove trailing comma
					String omimName = entry.substring(0, entry.length() - 6);
					omimName = omimName.replaceAll(", $", "");

					// write values to file
					Entity entity = new MapEntity();
					entity.set("identifier", identifier);
					entity.set("diseaseId", "OMIM:" + omimId);
					entity.set("diseaseName", omimName);
					entity.set("mappingMethod", mappingMethod);

					csvWriter.add(entity);
				}

				line = csvReader.readNext();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			IOUtils.closeQuietly(csvReader);
			IOUtils.closeQuietly(csvWriter);
		}
	}

	/**
	 * Combines a TYPICAL_FEATURES file with an ALL_FREQUENCIES file in the DiseaseMapping format and writes it to the
	 * specified output file. Make sure the two files are of the same source (OMIM, ORPHANET or ALL_SOURCES).
	 * 
	 * @param typicalFile
	 *            a TYPICAL_FEATURES mapping file
	 * @param allFile
	 *            an ALL_FREQUENCIES mapping file
	 * @param outFile
	 *            the combined pre-processed output file
	 */
	public void makeDiseaseMappingFile(File typicalFile, File allFile, File outFile)
	{
		CsvWriter csvWriter = null;
		CSVReader csvReaderTypical = null;
		CSVReader csvReaderAll = null;

		try
		{
			// instantiate readers and writers
			csvReaderTypical = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(typicalFile),
					CHARSET_UTF8)), '\t');
			csvReaderAll = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(allFile),
					CHARSET_UTF8)), '\t');

			csvWriter = new CsvWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile),
					CHARSET_UTF8)), '\t');

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
	 * Create the command line parameter options for this tool.
	 * 
	 * @return Options the command line options for this tool
	 */
	private Options createToolOptions()
	{
		Options options = new Options();
		options.addOption(new Option("help", false, "print this message"));

		Option outputPath = new Option("out", true, "Path to which the output file is written");
		outputPath.setRequired(true);
		options.addOption(outputPath);

		Option typicalMappingFile = new Option("typical", true,
				"Path to Disease/Gene/Phenotype TYPICAL_FEATURES mapping file");
		typicalMappingFile.setRequired(true);
		options.addOption(typicalMappingFile);

		Option allMappingFile = new Option("all", true, "Path to Disease/Gene/Phenotype ALL_FREQUENCIES mapping file");
		allMappingFile.setRequired(true);
		options.addOption(allMappingFile);

		Option morbidFile = new Option("morbidmap", true, "Path to morbidmap file");
		morbidFile.setRequired(true);
		options.addOption(morbidFile);

		return options;
	}

	/**
	 * Create the command line parameter for showing the help text.
	 * 
	 * @return Options the command line help option for this tool
	 */
	private Options createHelpOptions()
	{
		Options options = new Options();
		options.addOption(new Option("help", false, "print this message"));
		return options;
	}
}
