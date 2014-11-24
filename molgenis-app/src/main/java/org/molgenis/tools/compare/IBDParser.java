package org.molgenis.tools.compare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.support.MapEntity;

public class IBDParser
{

	// Manual:
	// Inputfiles can be found /groups/gcc/prm02/omxdata/IBDParelsnoer (sftp.gcc.rug.nl)
	// 1) Run the script
	// 2) Add to the outputfolder protocol.csv and dataset.csv
	// 3) Make zip file, when you are on a Mac go to step 3.1
	// 3.1) Go to the terminal and in the outputfolder type in: zip ibd.zip * -x "\.*"
	// Step 3.1 will not add hidden files like .MAC__OSX file
	public static void main(String[] args) throws IOException, InvalidFormatException
	{

		IBDParser vc = new IBDParser();
		String path = args[0];
		String inputFolder = path + "dataset/";

		String categoryFilePath = path + "category.csv";
		String featureFilePath = path + "observablefeature.csv";

		PrintWriter logfile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				new File(path + "logfile.txt")), Charset.forName("UTF-8")));
		try
		{
			File dir = new File(inputFolder);
			String datasetName = null;

			// Create map with key = tableName and value = the column name of the consultdate
			Map<String, String> consultMap = new HashMap<String, String>();
			consultMap.put("BB", "IDAABB");
			consultMap.put("BC", "IDAABC");
			consultMap.put("BE", "IDAABE");
			consultMap.put("BF", "IDAABF");
			consultMap.put("BG", "IDAABG");
			consultMap.put("BH", "IDAABH");
			consultMap.put("BI", "IDAABI");
			consultMap.put("BJ", "IDAABJ");
			consultMap.put("CA", "IDAABC");
			consultMap.put("CB", "IDAABC");
			consultMap.put("CD", "IDAABJ");
			consultMap.put("CE", "IDAABI");

			// Go through the files and check if there are multiple consultdates for a patient and filter then on first
			// consult date
			for (File child : dir.listFiles())
			{
				if (!child.isHidden())
				{
					String fileName = FilenameUtils.removeExtension(child.toString());
					String[] splitFileName = fileName.split("_");
					datasetName = splitFileName[2];
					if (!datasetName.equals("CD"))
					{
						vc.checkForMultipleConsults(datasetName, consultMap.get(datasetName), logfile);
					}
				}
			}
			// Filter categories: look for 'real' categories, this is because the data contains categories that are also
			// free text and our model cannot handle that.
			// There are also categories for date (when it is missing, in the data there are fake date formats as
			// 1808/08/08, we cannot handle that either
			vc.filterCategories(categoryFilePath, featureFilePath, logfile);
			System.out.println("logfile can be found at: " + path + "logfile.txt");
		}
		finally
		{
			logfile.close();
		}

	}

	public void checkForMultipleConsults(String datasetName, String consultDateName, PrintWriter logfile)
			throws IOException, InvalidFormatException
	{
		List<CellProcessor> processors = new ArrayList<CellProcessor>();
		processors.add(new TrimProcessor());
		String file2 = "/Users/Roan/Work/IBDParelsnoer/convert/dataset/dataset_VW_" + datasetName + "_.csv";

		CsvRepository csvRepository = new CsvRepository(new File(file2), processors);
		CsvRepository csvRepository2 = new CsvRepository(new File(file2), processors);
		CsvWriter csvWriter = new CsvWriter(new File("/Users/Roan/Work/IBDParelsnoer/convert/output/dataset_VW_"
				+ datasetName + "_.csv"));
		try
		{
			/*
			 * IDAA = identifier of patient {datasetname}_ID = row identifier IDAA{datasetName} = consultDate
			 */

			String lastIDAA = null;

			Map<String, String> mapID = new HashMap<String, String>();

			List<String> storedIDs = new ArrayList<String>();
			Iterable<String> listOfAttributeNames = null;
			String oldRowID = null;

			// Read the different dataset files

			for (Entity entity : csvRepository)
			{
				String id = entity.getString("IDAA");
				String consultDate = entity.getString(consultDateName);
				listOfAttributeNames = entity.getAttributeNames();

				if (!id.equals(lastIDAA))
				{
					mapID.put(id, consultDate);
					lastIDAA = id;
					oldRowID = entity.getString(datasetName + "_ID");
					storedIDs.add(oldRowID);
				}
				// Means that the IDAA is already there, the dates will now be compared in CompareDate()
				else
				{
					String storedID = compareDate(oldRowID, entity.getString(datasetName + "_ID"), mapID.get(id),
							consultDate, logfile);
					if (!storedIDs.contains(storedID)) storedIDs.add(storedID);
				}

			}

			// Now for the 2nd time the dataset files will be read
			// The rowIds that are stored in storedIDs are now used

			csvWriter.writeAttributeNames(listOfAttributeNames);
			Entity writeEntity = null;

			int counter = 0;
			for (Entity entity2 : csvRepository2)
			{
				counter++;
				writeEntity = new MapEntity();
				if (storedIDs.contains(entity2.getString(datasetName + "_ID")))
				{
					Iterator<String> it = entity2.getAttributeNames().iterator();
					while (it.hasNext())
					{
						String value = it.next();
						writeEntity.set(value, entity2.getString(value));
					}
					csvWriter.add(writeEntity);
				}

			}
			logfile.println("----------------------------------------------------------------");
			logfile.println(datasetName + " finished, total number of entities: " + counter);
		}
		finally
		{
			csvWriter.close();
			csvRepository.close();
			csvRepository2.close();
		}
	}

	private void filterCategories(String categoryFile, String featureFile, PrintWriter logfile)
			throws InvalidFormatException, IOException
	{
		logfile.println("########");
		logfile.println("Categories will be filtered");
		List<CellProcessor> processors = new ArrayList<CellProcessor>();
		processors.add(new TrimProcessor());
		CsvRepository repositoryCategoryCsvSourceInput = new CsvRepository(new File(categoryFile), processors);
		CsvRepository repositoryCategoryCsvSourceOutput = new CsvRepository(new File(categoryFile), processors);
		CsvRepository repositoryFeatureCsvSourceOutput = new CsvRepository(new File(featureFile), processors);
		try
		{
			Iterable<String> listOfAttributeNames = null;
			Map<String, List<String>> mapOfStrings = new HashMap<String, List<String>>();
			List<String> listOfStrings = new ArrayList<String>();

			for (Entity entity : repositoryCategoryCsvSourceInput)
			{
				listOfAttributeNames = entity.getAttributeNames();
				String feature = entity.getString("observablefeature_identifier");
				String valueCode = entity.getString("valuecode");

				if (!mapOfStrings.containsKey(feature))
				{
					listOfStrings = new ArrayList<String>();
					listOfStrings.add(valueCode);
					mapOfStrings.put(feature, listOfStrings);
				}
				else
				{
					mapOfStrings.get(feature).add(valueCode);
				}

			}
			List<String> listOfFeatures = new ArrayList<String>();
			for (Entry<String, List<String>> entry : mapOfStrings.entrySet())
			{
				boolean cat = false;
				for (String valueCode : entry.getValue())
				{
					// When it not starts with '-' and not contains '1808', '1809' or '1810' then it is a real category
					if (!valueCode.startsWith("-") && !valueCode.contains("1808") && !valueCode.contains("1809")
							&& !valueCode.contains("1810"))
					{
						cat = true;
					}
					if (cat)
					{
						listOfFeatures.add(entry.getKey());
						break;
					}
				}
			}

			// Update the category.csv
			writeCategoryOutput(listOfAttributeNames, repositoryCategoryCsvSourceOutput, listOfFeatures);
			logfile.println("category output done");

			// Update the observablefeature.csv
			writeFeatureOutput(listOfAttributeNames, repositoryFeatureCsvSourceOutput, listOfFeatures);
			logfile.println("feature output done");
		}
		finally
		{
			repositoryCategoryCsvSourceOutput.close();
			repositoryCategoryCsvSourceInput.close();
			repositoryFeatureCsvSourceOutput.close();
		}
	}

	private void writeCategoryOutput(Iterable<String> listOfAttributeNames, Repository categoryCsvOutput,
			List<String> listOfFeatures) throws IOException
	{
		CsvWriter csvWriter = new CsvWriter(new File("/Users/Roan/Work/IBDParelsnoer/convert/output/category.csv"));
		try
		{
			csvWriter.writeAttributeNames(listOfAttributeNames);
			Entity writeEntity = null;

			for (Entity entity2 : categoryCsvOutput)
			{
				writeEntity = new MapEntity();
				if (listOfFeatures.contains(entity2.getString("observablefeature_identifier")))
				{
					Iterator<String> it = entity2.getAttributeNames().iterator();
					while (it.hasNext())
					{
						String value = it.next();
						writeEntity.set(value, entity2.getString(value));
					}
					csvWriter.add(writeEntity);
				}

			}
		}
		finally
		{
			csvWriter.close();
		}
	}

	private void writeFeatureOutput(Iterable<String> listOfAttributeNames, Repository featureCsvInput,
			List<String> listOfFeatures) throws IOException
	{
		List<String> headers = Arrays.asList("identifier", "name", "dataType", "description");
		CsvWriter csvWriterFeature = new CsvWriter(new File(
				"/Users/Roan/Work/IBDParelsnoer/convert/output/observablefeature.csv"));
		try
		{
			csvWriterFeature.writeAttributeNames(headers);
			Entity writeEntityFeature = null;
			for (Entity entity : featureCsvInput)
			{
				writeEntityFeature = new MapEntity();
				if (listOfFeatures.contains(entity.getString("identifier")))
				{
					Iterator<String> it = entity.getAttributeNames().iterator();
					while (it.hasNext())
					{
						String value = it.next();
						writeEntityFeature.set(value, entity.getString(value));
					}
					csvWriterFeature.add(writeEntityFeature);
				}
				else if (entity.getString("dataType") == null || entity.getString("dataType").equals("categorical"))
				{
					writeEntityFeature.set("identifier", entity.getString("identifier"));
					writeEntityFeature.set("name", entity.getString("name"));
					writeEntityFeature.set("dataType", "string");
					writeEntityFeature.set("description", entity.getString("description"));
					csvWriterFeature.add(writeEntityFeature);

				}
				else
				{
					Iterator<String> it = entity.getAttributeNames().iterator();
					while (it.hasNext())
					{
						String value = it.next();
						writeEntityFeature.set(value, entity.getString(value));
					}
					csvWriterFeature.add(writeEntityFeature);
				}

			}
		}
		finally
		{
			csvWriterFeature.close();
		}
	}

	public String compareDate(String oldId, String newId, String date1, String date2, PrintWriter logfile)
	{
		if (!date1.contains("-") || !date2.contains("-"))
		{
			logfile.println("NO REAL DATES: " + oldId + ":" + date1 + "\t" + newId + ":" + date2);
			return date1;
		}
		String[] consult1 = date1.substring(0, 10).split("-");
		String[] consult2 = date2.substring(0, 10).split("-");

		int yearConsult1 = Integer.parseInt(consult1[0]);
		int yearConsult2 = Integer.parseInt(consult2[0]);
		int monthConsult1 = Integer.parseInt(consult1[1]);
		int monthConsult2 = Integer.parseInt(consult2[1]);
		int dayConsult1 = Integer.parseInt(consult1[2]);
		int dayConsult2 = Integer.parseInt(consult2[2]);

		// Check years
		if (yearConsult1 < yearConsult2)
		{
			return oldId;
		}
		else if (yearConsult1 > yearConsult2)
		{
			return newId;
		}

		// Check months
		if (monthConsult1 < monthConsult2)
		{
			return oldId;
		}
		else if (monthConsult1 > monthConsult2)
		{
			return newId;
		}

		// Check days
		if (dayConsult1 < dayConsult2)
		{
			return oldId;
		}
		else if (dayConsult1 > dayConsult2)
		{
			return newId;
		}

		else
		{
			logfile.println("Dates are identical. OldID:" + oldId + ":" + yearConsult1 + "-" + monthConsult1 + "-"
					+ dayConsult1 + "\t" + "NewID:" + newId + ":" + yearConsult2 + "-" + monthConsult2 + "-"
					+ dayConsult2);
			return oldId;

		}
	}
}
