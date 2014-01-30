package org.molgenis.sampletab;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntitySource;
import org.molgenis.data.Repository;
import org.molgenis.data.Writable;
import org.molgenis.data.WritableFactory;
import org.molgenis.data.excel.ExcelEntitySourceFactory;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.support.MapEntity;

public class SampleTabOmxConverter
{
	private final String submissionID;
	private final Map<String, String> unitOntologyTermsForFeatures;

	public SampleTabOmxConverter(String inputFilePath, String submissionID, String sheetName) throws IOException
	{
		this.submissionID = submissionID;
		this.unitOntologyTermsForFeatures = new HashMap<String, String>();

		EntitySource entitySource = new ExcelEntitySourceFactory().create(new File(inputFilePath));
		WritableFactory writableFactory = new ExcelWriter(new File(inputFilePath + ".Omx.xls"));

		try
		{
			Repository repo = entitySource.getRepositoryByEntityName(sheetName);
			try
			{
				// Collect headers as features to be imported in Omx-format
				List<String> listOfColumns = collectColumns(repo);
				// Collect observableFeatures
				List<String> listOfObservableFeatures = collectObservableFeatures(listOfColumns);
				addObserableFeatureTab(writableFactory, listOfObservableFeatures);
				addProtocolTab(writableFactory, listOfObservableFeatures);
				addDataSet(writableFactory);
				addSDataSetMatrix(writableFactory, repo, listOfObservableFeatures);
				addOntologyTermTab(writableFactory);
			}
			finally
			{
				repo.close();
			}
		}
		finally
		{
			writableFactory.close();
			entitySource.close();
		}
	}

	private void addOntologyTermTab(WritableFactory writableFactory) throws IOException
	{
		Writable ontologyTermSheet = writableFactory
				.createWritable("ontologyTerm", Arrays.asList("identifier", "name"));
		try
		{
			for (Entry<String, String> entry : unitOntologyTermsForFeatures.entrySet())
			{
				String ontologyTerm = entry.getValue();
				Entity newRow = new MapEntity();
				newRow.set("identifier", createIdentifier(ontologyTerm));
				newRow.set("name", ontologyTerm);
				ontologyTermSheet.add(newRow);
				unitOntologyTermsForFeatures.put(entry.getKey(), createIdentifier(ontologyTerm));
			}
		}
		finally
		{
			ontologyTermSheet.close();
		}
	}

	private void addObserableFeatureTab(WritableFactory writableFactory, List<String> listOfObservableFeatures)
			throws IOException
	{
		List<String> headers = Arrays.asList("identifier", "name", "unit_Identifier");
		Writable observableFeatureSheet = writableFactory.createWritable("observableFeature", headers);
		try
		{
			for (String eachFeature : listOfObservableFeatures)
			{
				eachFeature = pattenMatchExtractFeature(eachFeature);
				Entity newRow = new MapEntity();
				newRow.set("identifier", createIdentifier(eachFeature));
				newRow.set("name", eachFeature);

				observableFeatureSheet.add(newRow);
			}
		}
		finally
		{
			observableFeatureSheet.close();
		}
	}

	private void addProtocolTab(WritableFactory writableFactory, List<String> listOfObservableFeatures)
			throws IOException
	{
		List<String> headers = Arrays.asList("identifier", "name", "features_Identifier");
		Writable protocolSheet = writableFactory.createWritable("protocol", headers);
		try
		{
			Entity row = new MapEntity();
			row.set("identifier", submissionID + "-protocol");
			row.set("name", submissionID + "-protocol");

			StringBuilder featureIdentifier = new StringBuilder();
			for (String eachFeature : listOfObservableFeatures)
			{
				featureIdentifier.append(createIdentifier(pattenMatchExtractFeature(eachFeature))).append(',');
			}
			featureIdentifier.deleteCharAt(featureIdentifier.length() - 1);
			row.set("features_Identifier", featureIdentifier.toString());
			protocolSheet.add(row);
		}
		finally
		{
			protocolSheet.close();
		}
	}

	// Copy the values from one file to the other by using Entity
	private void addSDataSetMatrix(WritableFactory writableFactory, Repository inputSheet,
			List<String> listOfObservableFeatures) throws IOException
	{
		Map<String, String> headerMapper = new HashMap<String, String>();
		for (String originalHeader : listOfObservableFeatures)
			headerMapper.put(originalHeader, createIdentifier(pattenMatchExtractFeature(originalHeader)));

		Writable writable = writableFactory.createWritable("dataset_" + submissionID + "-dataset",
				new ArrayList<String>(headerMapper.values()));
		try
		{

			for (Entity entity : inputSheet)
			{
				Entity newRow = new MapEntity();
				for (String eachField : entity.getAttributeNames())
				{
					if (headerMapper.containsKey(eachField))
					{
						String value = entity.getString(eachField);
						newRow.set(headerMapper.get(eachField), value);
					}
				}
				writable.add(newRow);
			}
		}
		finally
		{
			writable.close();
		}
	}

	private void addDataSet(WritableFactory writableFactory) throws IOException
	{
		Writable writable = writableFactory.createWritable("dataset",
				Arrays.asList("identifier", "name", "protocolused_identifier"));
		try
		{
			Entity dataSetRow = new MapEntity();
			dataSetRow.set("identifier", submissionID + "-dataset");
			dataSetRow.set("name", submissionID + "-dataset");
			dataSetRow.set("protocolused_identifier", submissionID + "-protocol");
			writable.add(dataSetRow);
		}
		finally
		{
			writable.close();
		}
	}

	private List<String> collectObservableFeatures(List<String> listOfColumns)
	{
		List<String> listOfObservableFeatures = new ArrayList<String>();
		for (String currentColumn : listOfColumns)
		{
			if (!currentColumn.toLowerCase().startsWith("unit"))
			{
				String nextColumn = null;
				int currentIndex = listOfColumns.indexOf(currentColumn);
				if (currentIndex + 1 < listOfColumns.size()) nextColumn = listOfColumns.get(currentIndex + 1);
				if (nextColumn != null && nextColumn.toLowerCase().startsWith("unit"))
				{
					unitOntologyTermsForFeatures.put(currentColumn, nextColumn);
				}
				listOfObservableFeatures.add(currentColumn);
			}
		}
		return listOfObservableFeatures;
	}

	private List<String> collectColumns(Repository repo) throws IOException
	{
		List<String> listOfFeatures = new ArrayList<String>();
		for (AttributeMetaData attr : repo.getAttributes())
		{
			listOfFeatures.add(attr.getName());
		}
		return listOfFeatures;
	}

	private String pattenMatchExtractFeature(String originalName)
	{
		Pattern pattern = Pattern.compile(".+\\[(.+)\\]");
		Matcher matcher = pattern.matcher(originalName);
		if (matcher.find())
		{
			originalName = matcher.group(1);
		}
		return originalName;
	}

	private String createIdentifier(String originalName)
	{
		StringBuilder identifiier = new StringBuilder();
		identifiier.append(submissionID).append('.').append(originalName.replaceAll(" ", ""));
		return identifiier.toString();
	}

	public static void main(String args[]) throws IOException
	{
		new SampleTabOmxConverter(args[0], "GCR-ada", args[1]);
		if (args.length != 2)
		{
			System.err.println("Usage: <sample_data.xlsx> <sheetname>");
			return;
		}

	}
}