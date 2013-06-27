package org.molgenis.gids.tools.convertor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
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
	private static String OUTPUTDIR = null;
	private static String PROJECT = null;
	private static String IDENTIFIER = "id_sample";
	private List<String> featureColNames = null;
	MakeEntityNameAndIdentifier mkObsProtocol = null;
	private HashMap<String, HashSet<String>> hashMapCategories = new HashMap<String, HashSet<String>>();
	MakeEntityNameAndIdentifier mkObsFeature = null;
	List<MakeEntityNameAndIdentifier> mkObsProtocollist = new ArrayList<MakeEntityNameAndIdentifier>();
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
		listOfEntity.add("protocol");
		listOfEntity.add("observableFeature");
		listOfEntity.add("dataset_" + PROJECT.toLowerCase().trim());

		try
		{
			for (TupleReader sheetReader : excelReader)
			{
				this.featureColNames = new ArrayList<String>();
				for (Iterator<String> it = sheetReader.colNamesIterator(); it.hasNext();)
				{
					String colName = it.next();
					if (colName.equals(IDENTIFIER))
					{
						this.featureColNames.add(0, colName);
					}
					else
					{
						this.featureColNames.add(colName);
					}
				}

				csvWriter.writeColNames(this.featureColNames);
				for (Iterator<Tuple> it = sheetReader.iterator(); it.hasNext();)
				{
					Tuple row = it.next();
					// If the sample name exists
					if (row.getString(IDENTIFIER) != null && !row.getString(IDENTIFIER).isEmpty())
					{
						String sampleId = row.getString(IDENTIFIER);
						if (checkIfDouble(sampleId))
						{
							System.out.println("Double entry: " + sampleId + " has been removed");

						}
						else
						{
							createCategoryList(row, sampleId);

							csvWriter.write(row);
						}
					}
					else
					{
						WritableTuple tup = new KeyValueTuple();
						for (String featureColName : featureColNames)
						{
							if (featureColName.equals(IDENTIFIER))
							{
								// Make a identifier for this sample
								tup.set(featureColName, emptySample());
							}
							else
							{
								tup.set(featureColName, row.getString(featureColName));
							}

						}
						csvWriter.write(tup);
					}
				}
			}
			makeProtocolList(this.featureColNames);
			if (this.featureColNames != null)
			{
				makeFeaturesList();
			}
			mkmetadataExcelFile(listOfEntity);

			for (Entry<String, HashSet<String>> entry : hashMapCategories.entrySet())
			{
				if (entry.getValue().size() > 1 && entry.getValue().size() < 100)
				{
					System.out.println(entry.getKey());
					for (String e : entry.getValue())
					{
						System.out.print(e + "^");
					}
					System.out.println("########");
				}

			}

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

	private void createCategoryList(Tuple row, String sampleId)
	{
		for (String feature : this.featureColNames)
		{
			HashSet<String> hashset = this.hashMapCategories.get(feature);
			if (hashMapCategories.get(feature) == null)
			{
				hashMapCategories.put(feature, new HashSet<String>());
			}
			else
			{
				hashset.add(row.getString(feature));
				hashMapCategories.put(feature, hashset);
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
		String sample = "unknown";
		return sample;
	}

	private void makeProtocolList(List<String> listOfFeatures)
	{
		String prot = "protocol_" + PROJECT;
		StringBuilder build = new StringBuilder();
		for (String e : listOfFeatures)
		{
			build.append(e + ",");
		}
		String features = build.substring(0, build.length() - 1);

		mkObsProtocol = new MakeEntityNameAndIdentifier(prot, prot, features);
		mkObsProtocollist.add(mkObsProtocol);
	}

	private void makeFeaturesList()
	{
		for (String featureColName : featureColNames)
		{
			mkObsFeature = new MakeEntityNameAndIdentifier(featureColName, featureColName, null);
			mkObsFeaturelist.add(mkObsFeature);
		}
	}

	public void mkMetadataFileProtocol(ExcelWriter excelWriterMD, String sheetName) throws IOException
	{
		TupleWriter esw = excelWriterMD.createTupleWriter("protocol");
		esw.writeColNames(Arrays.asList("identifier", "name", "features_identifier"));

		for (MakeEntityNameAndIdentifier i : mkObsProtocollist)
		{
			WritableTuple kvt = new KeyValueTuple();
			kvt.set("identifier", i.getIdentifier());
			kvt.set("name", i.getName());
			kvt.set("features_identifier", i.getFeatures_Identifier());
			esw.write(kvt);
		}
	}

	public void mkMetadataFileObservableFeature(ExcelWriter excelWriterMD, String sheetName) throws IOException
	{
		TupleWriter esw = excelWriterMD.createTupleWriter("observableFeature");
		esw.writeColNames(Arrays.asList("identifier", "name"));

		for (MakeEntityNameAndIdentifier m : mkObsFeaturelist)
		{
			WritableTuple kvt = new KeyValueTuple();
			kvt.set("identifier", m.getIdentifier());
			kvt.set("name", m.getName());
			esw.write(kvt);
		}
	}

	public void mkMetadataFileDataSet(ExcelWriter excelWriterMD, String sheetName) throws IOException
	{
		TupleWriter esw = excelWriterMD.createTupleWriter(sheetName);
		esw.writeColNames(Arrays.asList("identifier", "name", "protocolused_identifier"));
		WritableTuple kvt = new KeyValueTuple();
		kvt.set("protocolused_identifier", "protocol_" + PROJECT);
		kvt.set("identifier", PROJECT.toLowerCase());
		kvt.set("name", PROJECT.toLowerCase());
		esw.write(kvt);
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
				mkMetadataFileDataSet(excelWriterMD, sheetName);
			}
			else if (sheetName.equals("protocol"))
			{

				mkMetadataFileProtocol(excelWriterMD, sheetName);

			}
			else if (sheetName.equals("observableFeature"))
			{

				mkMetadataFileObservableFeature(excelWriterMD, sheetName);
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
