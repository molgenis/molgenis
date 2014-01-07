package org.molgenis.gids.tools.convertor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.Writable;
import org.molgenis.data.WritableFactory;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.excel.ExcelEntitySource;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.excel.ExcelWriter.FileFormat;
import org.molgenis.data.processor.LowerCaseProcessor;
import org.molgenis.data.processor.TrimProcessor;
import org.molgenis.data.support.MapEntity;

public class SampleConverter
{

	private final Set<String> listOfDoubleSamples = new HashSet<String>();
	private String OUTPUTDIR = null;
	private String PROJECT = null;
	private String IDENTIFIER = "id_sample";
	private List<String> featureColNames = null;
	MakeEntityNameAndIdentifier mkObsProtocol = null;
	private final HashMap<String, HashSet<String>> hashMapCategories = new HashMap<String, HashSet<String>>();
	MakeEntityNameAndIdentifier mkObsFeature = null;
	List<MakeEntityNameAndIdentifier> mkObsProtocollist = new ArrayList<MakeEntityNameAndIdentifier>();
	List<MakeEntityNameAndIdentifier> mkObsFeaturelist = new ArrayList<MakeEntityNameAndIdentifier>();

	public void convert(InputStream in, OutputStream out, String outputdir, String projectName) throws IOException
	{

		OUTPUTDIR = outputdir;
		PROJECT = projectName;

		ExcelEntitySource entitySource = new ExcelEntitySource(in, null);
		entitySource.addCellProcessor(new TrimProcessor(false, true));

		CsvWriter<Entity> csvWriter = null;

		ArrayList<String> listOfEntity = new ArrayList<String>();
		listOfEntity.add("dataset");
		listOfEntity.add("protocol");
		listOfEntity.add("observableFeature");
		listOfEntity.add("dataset_" + PROJECT.toLowerCase().trim());

		try
		{
			for (String entityName : entitySource.getEntityNames())
			{
				Repository<? extends Entity> sheetReader = entitySource.getRepositoryByEntityName(entityName);
				this.featureColNames = new ArrayList<String>();

				for (AttributeMetaData attr : sheetReader.getAttributes())
				{
					String colName = attr.getName();
					if (colName.equals(IDENTIFIER))
					{
						this.featureColNames.add(0, colName);
					}
					else
					{
						this.featureColNames.add(colName);
					}
				}

				csvWriter = new CsvWriter<Entity>(new OutputStreamWriter(out, Charset.forName("UTF-8")),
						this.featureColNames);

				for (Entity entity : sheetReader)
				{
					// If the sample name exists
					if (entity.getString(IDENTIFIER) != null && !entity.getString(IDENTIFIER).isEmpty())
					{
						String sampleId = entity.getString(IDENTIFIER);
						if (!checkIfDouble(sampleId))
						{
							createCategoryList(entity, sampleId);
							csvWriter.add(entity);
						}

					}
					else
					{
						Entity entOut = new MapEntity();
						for (String featureColName : featureColNames)
						{
							if (featureColName.equals(IDENTIFIER))
							{
								// Make a identifier for this sample
								entOut.set(featureColName, emptySample());
							}
							else
							{
								entOut.set(featureColName, entity.getString(featureColName));
							}

						}
						csvWriter.add(entOut);
					}
				}
			}
			makeProtocolList(this.featureColNames);
			if (this.featureColNames != null)
			{
				makeFeaturesList();
			}
			mkmetadataExcelFile(listOfEntity);
			// Write categories to file
			PrintWriter printCategories = new PrintWriter(new File(OUTPUTDIR + "/categories.txt"));
			for (Entry<String, HashSet<String>> entry : hashMapCategories.entrySet())
			{
				if (entry.getValue().size() > 1 && entry.getValue().size() < 100)
				{
					printCategories.append(entry.getKey() + "^");
					for (String e : entry.getValue())
					{
						printCategories.append(e + "^");
					}
					printCategories.append("\n");
				}

			}
			printCategories.close();

		}
		finally
		{
			try
			{
				entitySource.close();
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

	private void createCategoryList(Entity entity, String sampleId)
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
				hashset.add(entity.getString(feature));
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

	public void mkMetadataFileProtocol(WritableFactory<Entity> writableFactoryMD, String sheetName) throws IOException
	{
		Writable<Entity> esw = writableFactoryMD.createWritable("protocol",
				Arrays.asList("identifier", "name", "features_identifier"));

		for (MakeEntityNameAndIdentifier i : mkObsProtocollist)
		{
			Entity kvt = new MapEntity();
			kvt.set("identifier", i.getIdentifier());
			kvt.set("name", i.getName());
			kvt.set("features_identifier", i.getFeatures_Identifier());
			esw.add(kvt);
		}
	}

	public void mkMetadataFileObservableFeature(WritableFactory<Entity> writableFactoryMD, String sheetName)
			throws IOException
	{
		Writable<Entity> esw = writableFactoryMD.createWritable("observableFeature",
				Arrays.asList("identifier", "name"));

		for (MakeEntityNameAndIdentifier m : mkObsFeaturelist)
		{
			Entity kvt = new MapEntity();
			kvt.set("identifier", m.getIdentifier());
			kvt.set("name", m.getName());
			esw.add(kvt);
		}
	}

	public void mkMetadataFileDataSet(WritableFactory<Entity> writableFactoryMD, String sheetName) throws IOException
	{
		Writable<Entity> esw = writableFactoryMD.createWritable(sheetName,
				Arrays.asList("identifier", "name", "protocolused_identifier"));

		Entity kvt = new MapEntity();
		kvt.set("protocolused_identifier", "protocol_" + PROJECT);
		kvt.set("identifier", PROJECT.toLowerCase());
		kvt.set("name", PROJECT.toLowerCase());
		esw.add(kvt);
	}

	public void mkmetadataExcelFile(ArrayList<String> listOfEntity) throws IOException
	{

		OutputStream osMD = new FileOutputStream(OUTPUTDIR + PROJECT + "_metadata.xls");

		ExcelWriter<Entity> excelWriterMD = new ExcelWriter<Entity>(osMD, FileFormat.XLS);
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
				excelWriterMD.createWritable(sheetName, Collections.<String> emptyList());
			}
		}
		excelWriterMD.close();

	}

}
