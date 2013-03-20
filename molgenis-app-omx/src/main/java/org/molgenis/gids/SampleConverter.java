package org.molgenis.gids;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.io.csv.CsvWriter;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.io.excel.ExcelWriter.FileFormat;
import org.molgenis.io.processor.LowerCaseProcessor;
import org.molgenis.io.processor.TrimProcessor;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

public class SampleConverter
{

	private final Set<String> listOfDoubleSamples = new HashSet<String>();
	private int counter = 0;
	// private Map<String, String> observationTargetMap = new HashMap<String,
	// String>();

	private static String OUTPUTDIR = null;
	private static String PROJECT = null;
	private List<String> featureColNames = null;
	MakeEntityNameAndIdentifier mkObsTarget = null;
	MakeEntityNameAndIdentifier mkObsFeature = null;
	List<MakeEntityNameAndIdentifier> mkObsTargetlist = new ArrayList<MakeEntityNameAndIdentifier>();
	List<MakeEntityNameAndIdentifier> mkObsFeaturelist = new ArrayList<MakeEntityNameAndIdentifier>();

	public void convert(InputStream in, OutputStream out, String outputdir, String projectName) throws IOException
	{

		OUTPUTDIR = outputdir;
		PROJECT = projectName;
		ExcelReader excelReader = new ExcelReader(in);
		excelReader.addCellProcessor(new TrimProcessor(false, true));
		TupleWriter csvWriter = new CsvWriter(new OutputStreamWriter(out, Charset.forName("UTF-8")));
		csvWriter.addCellProcessor(new LowerCaseProcessor(true, false));
		ArrayList<String> listOfEntity = new ArrayList<String>();
		listOfEntity.add("dataset");
		listOfEntity.add("observationtarget");
		listOfEntity.add("observableFeature");
		listOfEntity.add("dataset_" + PROJECT.toLowerCase().trim());

		mkmetadataExcelFile(listOfEntity);

		try
		{
			// write data
			for (TupleReader sheetReader : excelReader)
			{
				featureColNames = new ArrayList<String>();
				for (Iterator<String> it = sheetReader.colNamesIterator(); it.hasNext();)
				{
					String colName = it.next();
					if (colName.equals("id_sample")) featureColNames.add(0, colName);
					else featureColNames.add(colName);
				}

				csvWriter.writeColNames(featureColNames);
				for (Iterator<Tuple> it = sheetReader.iterator(); it.hasNext();)
				{
					Tuple row = it.next();
					// If the sample name exists
					if (row.getString("id_sample") != null && !row.getString("id_sample").isEmpty())
					{
						String sampleId = row.getString("id_sample");
						if (checkIfDouble(sampleId))
						{
							System.out.println("Double entry: " + sampleId + " has been removed");

						}
						else
						{
							// Get the targets for metadatafile
							mkObsTarget = new MakeEntityNameAndIdentifier(sampleId, sampleId);
							mkObsTargetlist.add(mkObsTarget);
							// Write the real data
							csvWriter.write(row);
						}
					}
					else
					{
						WritableTuple tup = new KeyValueTuple();
						for (String featureColName : featureColNames)
						{
							if (featureColName.equals("id_sample"))
							{
								// Make a identifier for this sample
								tup.set(featureColName, emptySample());
							}
							else
							{
								tup.set(featureColName, row.getString(featureColName));
							}

						}
						// The identifier and sample are now set for this former
						// empty sample name
						csvWriter.write(tup);
					}
				}
			}
			// Fill a list with all the ObservableFeatures in the file
			if (featureColNames != null)
			{
				makeFeaturesList();
			}

			// Write the metadata to a file for the features
			mkMetadataFileObservableFeature();
			// Write the metadata to a file for the targets
			mkMetadataFileObservationTarget();

		}
		finally
		{
			try
			{
				excelReader.close();
			}
			catch (IOException e)
			{
			}
			try
			{
				csvWriter.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	public boolean checkIfDouble(String sample)
	{

		if (!listOfDoubleSamples.contains(sample))
		{
			listOfDoubleSamples.add(sample);
			return false;
		}
		else
		{
			return true;
		}
	}

	public String emptySample()
	{
		String sample = "Dummy_2012_" + PROJECT + "_" + (++counter);
		mkObsTarget = new MakeEntityNameAndIdentifier("unknown", sample);
		mkObsTargetlist.add(mkObsTarget);
		return sample;
	}

	public void mkMetadataFileObservationTarget() throws IOException
	{

		OutputStream osMD = new FileOutputStream(OUTPUTDIR + PROJECT + "_MetaDataObservationTarget.csv");

		TupleWriter csvWriterMD = new CsvWriter(new OutputStreamWriter(osMD, Charset.forName("UTF-8")));
		csvWriterMD.addCellProcessor(new LowerCaseProcessor(true, false));
		csvWriterMD.writeColNames(Arrays.asList("identifier", " name"));

		for (MakeEntityNameAndIdentifier i : mkObsTargetlist)
		{
			WritableTuple kvt = new KeyValueTuple();
			kvt.set("identifier", i.getIdentifier());
			kvt.set("name", i.getName());
			csvWriterMD.write(kvt);
		}
		csvWriterMD.close();

	}

	private void makeFeaturesList()
	{
		for (String featureColName : featureColNames)
		{
			mkObsFeature = new MakeEntityNameAndIdentifier(featureColName, featureColName);
			mkObsFeaturelist.add(mkObsFeature);
		}

	}

	public void mkMetadataFileObservableFeature() throws IOException
	{

		OutputStream osMD = new FileOutputStream(OUTPUTDIR + PROJECT + "_MetaDataObservableFeature.csv");

		TupleWriter csvWriterMD = new CsvWriter(new OutputStreamWriter(osMD, Charset.forName("UTF-8")));
		csvWriterMD.addCellProcessor(new LowerCaseProcessor(true, false));
		csvWriterMD.writeColNames(Arrays.asList("identifier", "name"));
		for (MakeEntityNameAndIdentifier m : mkObsFeaturelist)
		{
			WritableTuple kvt = new KeyValueTuple();
			kvt.set("identifier", m.getIdentifier());
			kvt.set("name", m.getName());
			csvWriterMD.write(kvt);
		}
		csvWriterMD.close();
	}

	public void mkmetadataExcelFile(ArrayList<String> listOfEntity) throws IOException
	{

		OutputStream osMD = new FileOutputStream(OUTPUTDIR + PROJECT + "_metadata.xls");

		ExcelWriter excelWriterMD = new ExcelWriter(osMD, FileFormat.XLS);
		excelWriterMD.addCellProcessor(new LowerCaseProcessor(true, false));

		for (String sheetName : listOfEntity)
		{
			if (sheetName.equals("dataset"))
			{
				TupleWriter esw = excelWriterMD.createTupleWriter("dataset");
				esw.writeColNames(Arrays.asList("identifier", "name", "protocolused_identifier"));
				WritableTuple kvt = new KeyValueTuple();
				kvt.set("protocolused_identifier", "");
				kvt.set("identifier", PROJECT.toLowerCase());
				kvt.set("name", PROJECT.toLowerCase());
				esw.write(kvt);
			}
			else
			{
				// create empty sheet
				excelWriterMD.createTupleWriter(sheetName);
			}
		}
		excelWriterMD.close();

	}

}
