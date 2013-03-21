package org.molgenis.sampletab;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.io.excel.ExcelSheetWriter;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;

public class SampleTabOmxConverter
{
	private String submissionID;
	private Map<String, String> unitOntologyTermsForFeatures;
	private Map<String, String> linkForFeatureToUnits;

	public SampleTabOmxConverter(String inputFilePath, String submissionID) throws IOException
	{
		this.submissionID = submissionID;
		this.unitOntologyTermsForFeatures = new HashMap<String, String>();
		this.linkForFeatureToUnits = new HashMap<String, String>();
		ExcelReader reader = new ExcelReader(new File(inputFilePath));
		ExcelSheetReader sheet = reader.getSheet(0);
		// Collect headers as features to be imported in Omx-format
		List<String> listOfColumns = collectColumns(sheet);
		// Collect observableFeatures and ontology terms, but ontology terms
		// reference is missing from observableFeature entity
		// at the moment, so therefore we treat all the columns as features
		// only!
		List<String> listOfObservableFeatures = collectObservableFeatures(listOfColumns);
		ExcelWriter writer = new ExcelWriter(new File(inputFilePath + ".Omx.xls"));
		// Add protocol tab
		addProtocolTab(writer, listOfObservableFeatures);
		// Add dataset tab
		addDataSet(writer);
		// Add data matrix tab
		addSDataSetMatrix(writer, sheet, listOfObservableFeatures);
		// Add ontology term tab
		addOntologyTermTab(writer);
		// Add observable feature tab
		addObservableFeatureTab(writer, listOfObservableFeatures);
		reader.close();
		writer.close();

		System.out.println("The sample tab data has been converted into Omx format!");
	}

	public void addOntologyTermTab(ExcelWriter writer) throws IOException
	{
		ExcelSheetWriter ontologyTermSheet = (ExcelSheetWriter) writer.createTupleWriter("ontologyTerm");
		List<String> headers = Arrays.asList("identifier", "name");
		ontologyTermSheet.writeColNames(headers);
		for (Entry<String, String> entry : linkForFeatureToUnits.entrySet())
		{
			String ontologyTerm = entry.getValue();
			KeyValueTuple newRow = new KeyValueTuple();
			newRow.set("identifier", createIdentifier(ontologyTerm));
			newRow.set("name", ontologyTerm);
			ontologyTermSheet.write(newRow);
			linkForFeatureToUnits.put(entry.getKey(), createIdentifier(ontologyTerm));
		}
		ontologyTermSheet.close();
	}

	public void addObservableFeatureTab(ExcelWriter writer, List<String> listOfObservableFeatures) throws IOException
	{
		ExcelSheetWriter observableFeatureSheet = (ExcelSheetWriter) writer.createTupleWriter("observableFeature");
		List<String> headers = Arrays.asList("identifier", "name", "unit_Identifier");
		observableFeatureSheet.writeColNames(headers);
		for (String eachFeature : listOfObservableFeatures)
		{
			KeyValueTuple newRow = new KeyValueTuple();
			if (linkForFeatureToUnits.containsKey(eachFeature))
			{
				newRow.set("unit_Identifier", linkForFeatureToUnits.get(eachFeature));
			}
			eachFeature = pattenMatchExtractFeature(eachFeature);
			newRow.set("identifier", createIdentifier(eachFeature));
			newRow.set("name", eachFeature);
			observableFeatureSheet.write(newRow);
		}
		observableFeatureSheet.close();
	}

	public void addProtocolTab(ExcelWriter writer, List<String> listOfObservableFeatures) throws IOException
	{
		ExcelSheetWriter protocolSheet = (ExcelSheetWriter) writer.createTupleWriter("protocol");
		List<String> headers = Arrays.asList("identifier", "name", "features_Identifier");
		protocolSheet.writeColNames(headers);
		KeyValueTuple row = new KeyValueTuple();
		row.set("identifier", submissionID + "-protocol");
		row.set("name", submissionID + "-protocol");

		StringBuilder featureIdentifier = new StringBuilder();
		for (String eachFeature : listOfObservableFeatures)
		{
			featureIdentifier.append(createIdentifier(pattenMatchExtractFeature(eachFeature))).append(',');
		}
		featureIdentifier.deleteCharAt(featureIdentifier.length() - 1);
		row.set("features_Identifier", featureIdentifier.toString());
		protocolSheet.write(row);
		protocolSheet.close();
	}

	// Copy the values from one file to the other by using Tuple
	public void addSDataSetMatrix(ExcelWriter writer, ExcelSheetReader inputSheet, List<String> listOfObservableFeatures)
			throws IOException
	{
		ExcelSheetWriter dataSetSheetMatrix = (ExcelSheetWriter) writer.createTupleWriter("dataset_" + submissionID
				+ "-dataset");
		Map<String, String> headerMapper = new HashMap<String, String>();
		for (String originalHeader : listOfObservableFeatures)
			headerMapper.put(originalHeader, createIdentifier(pattenMatchExtractFeature(originalHeader)));

		dataSetSheetMatrix.writeColNames(headerMapper.values());

		Iterator<Tuple> inputRows = inputSheet.iterator();
		while (inputRows.hasNext())
		{
			Tuple eachRow = inputRows.next();
			KeyValueTuple newRow = new KeyValueTuple();
			for (String eachField : eachRow.getColNames())
			{
				if (headerMapper.containsKey(eachField))
				{
					String value = eachRow.getString(eachField);
					newRow.set(headerMapper.get(eachField), value);
				}
				if (unitOntologyTermsForFeatures.containsKey(eachField))
				{
					String columnNameForUnits = unitOntologyTermsForFeatures.get(eachField);
					String unitValue = eachRow.getString(columnNameForUnits);
					if (unitValue != null) linkForFeatureToUnits.put(eachField, unitValue);
				}
			}

			dataSetSheetMatrix.write(newRow);
		}
		dataSetSheetMatrix.close();
	}

	public void addDataSet(ExcelWriter writer) throws IOException
	{
		ExcelSheetWriter dataSetSheet = (ExcelSheetWriter) writer.createTupleWriter("dataset");
		List<String> datatSetHeaders = Arrays.asList("identifier", "name", "protocolused_identifier");
		dataSetSheet.writeColNames(datatSetHeaders);
		KeyValueTuple dataSetRow = new KeyValueTuple();
		dataSetRow.set("identifier", submissionID + "-dataset");
		dataSetRow.set("name", submissionID + "-dataset");
		dataSetRow.set("protocolused_identifier", submissionID + "-protocol");
		dataSetSheet.write(dataSetRow);
		dataSetSheet.close();
	}

	public List<String> collectOntologyTerms(List<String> listOfColumns)
	{
		List<String> listOfOntologyTerms = new ArrayList<String>();
		for (String currentColumn : listOfColumns)
		{
			if (currentColumn.toLowerCase().startsWith("characteristic"))
			{
				String nextColumn = null;
				int currentIndex = listOfColumns.indexOf(currentColumn);
				if (currentIndex + 1 < listOfColumns.size()) nextColumn = listOfColumns.get(currentIndex + 1);

				if (nextColumn == null || !nextColumn.toLowerCase().startsWith("unit"))
				{
					Pattern pattern = Pattern.compile(".+\\[(.+)\\]");
					Matcher matcher = pattern.matcher(currentColumn);
					if (matcher.find()) currentColumn = matcher.group(1);
					listOfOntologyTerms.add(currentColumn);
				}
				// In this case, the Unit should be ontologyTerm that describes
				// the feature
				else unitOntologyTermsForFeatures.put(currentColumn, nextColumn);
			}
		}
		return listOfOntologyTerms;
	}

	public List<String> collectObservableFeatures(List<String> listOfColumns)
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

	public List<String> collectColumns(ExcelSheetReader sheet) throws IOException
	{
		List<String> listOfFeatures = new ArrayList<String>();
		Iterator<String> columnNames = sheet.colNamesIterator();
		while (columnNames.hasNext())
		{
			listOfFeatures.add(columnNames.next());
		}
		return listOfFeatures;
	}

	public String pattenMatchExtractFeature(String originalName)
	{
		Pattern pattern = Pattern.compile(".+\\[(.+)\\]");
		Matcher matcher = pattern.matcher(originalName);
		if (matcher.find())
		{
			originalName = matcher.group(1);
		}
		return originalName;
	}

	public String createIdentifier(String originalName)
	{
		StringBuilder identifiier = new StringBuilder();
		identifiier.append(submissionID).append('.').append(originalName.replaceAll(" ", ""));
		return identifiier.toString();
	}

	public static void main(String args[]) throws IOException
	{
		if (args.length != 2) System.err.println("Usage: <sample_data.xlsx> submission-id ");
		else new SampleTabOmxConverter(args[0], args[1]);
	}
}
