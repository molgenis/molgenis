package org.molgenis.omx.harmonization.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.io.excel.ExcelSheetWriter;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;

public class OpalToOMXConvertor
{
	private Map<String, List<UniqueCategory>> categoryInfo = new LinkedHashMap<String, List<UniqueCategory>>();
	private Map<String, UniqueVariable> variableInfo = new LinkedHashMap<String, UniqueVariable>();
	private String studyName = null;

	public OpalToOMXConvertor(String studyName, String filePath) throws IOException
	{
		this.studyName = studyName;
		start(filePath);
	}

	private void start(String fileString) throws IOException
	{
		File file = new File(fileString);
		if (file.exists())
		{
			ExcelReader reader = null;
			try
			{
				reader = new ExcelReader(file, true);
				// Handle category sheet first
				collectCategoryInfo(reader);
				// Handle variable sheet second
				collectVariableInfo(reader);
			}
			finally
			{
				if (reader != null) reader.close();
			}

			ExcelWriter writer = null;
			try
			{
				writer = new ExcelWriter(new File(file.getAbsolutePath() + ".OPAL.xls"));
				writeToPhenoFormat(writer);
			}
			finally
			{
				writer.close();
			}
		}
	}

	private void writeToPhenoFormat(ExcelWriter writer) throws IOException
	{
		String protocolIdentifier = studyName + "-protocol";
		String listOfFeatureIdentifier = "";
		// Create sheet for investigation
		ExcelSheetWriter dataSet = (ExcelSheetWriter) writer.createTupleWriter("dataset");
		dataSet.writeColNames(Arrays.asList("identifier", "name", "protocolUsed_identifier"));
		KeyValueTuple row = new KeyValueTuple();
		row.set("identifier", studyName);
		row.set("name", studyName);
		row.set("protocolUsed_identifier", protocolIdentifier);
		dataSet.write(row);

		// Create sheet for category
		ExcelSheetWriter categorySheet = (ExcelSheetWriter) writer.createTupleWriter("category");
		categorySheet.writeColNames(Arrays.asList("identifier", "name", "valueCode", "observablefeature_identifier"));

		for (Entry<String, List<UniqueCategory>> entry : categoryInfo.entrySet())
		{
			String featureIdentifier = studyName + "-feature-" + entry.getKey();
			List<UniqueCategory> categoriesPerFeature = entry.getValue();
			for (UniqueCategory eachCategory : categoriesPerFeature)
			{
				KeyValueTuple eachRow = new KeyValueTuple();
				eachRow.set("identifier", studyName + "-category-" + eachCategory.getName());
				eachRow.set("name", eachCategory.getLabel());
				eachRow.set("valueCode", eachCategory.getCode());
				eachRow.set("observablefeature_identifier", featureIdentifier);
				categorySheet.write(eachRow);
			}
		}

		// Create sheet for variable
		ExcelSheetWriter featureWriter = (ExcelSheetWriter) writer.createTupleWriter("observablefeature");
		featureWriter.writeColNames(Arrays.asList("identifier", "name", "description", "description", "dataType",
				"unit_Identifier"));
		for (UniqueVariable eachVariable : variableInfo.values())
		{
			KeyValueTuple eachRow = new KeyValueTuple();
			String name = eachVariable.getVariable();
			String dataType = eachVariable.getDataType();
			StringBuilder categoriesName = new StringBuilder();
			if (categoryInfo.containsKey(name))
			{
				for (UniqueCategory eachCategory : categoryInfo.get(name))
				{
					categoriesName.append(eachCategory.getName()).append(',');
				}
				dataType = "categorical";
			}
			else if (dataType != null && dataType.equals("integer")) dataType = "int";
			else if (dataType != null && dataType.equals("decimal")) dataType = "decimal";
			else dataType = "string";

			String featureIdentifier = studyName + "-feature-" + name;
			listOfFeatureIdentifier += featureIdentifier + ",";
			eachRow.set("identifier", featureIdentifier);
			eachRow.set("name", name);
			eachRow.set("description", eachVariable.getLabel());
			eachRow.set("dataType", dataType);
			featureWriter.write(eachRow);
		}

		// Create sheet for protocol
		ExcelSheetWriter protocolSheet = (ExcelSheetWriter) writer.createTupleWriter("protocol");
		protocolSheet.writeColNames(Arrays.asList("identifier", "name", "features_identifier",
				"subprotocols_identifier"));
		KeyValueTuple protocol = new KeyValueTuple();
		protocol.set("identifier", protocolIdentifier);
		protocol.set("name", protocolIdentifier);
		protocol.set("features_identifier", listOfFeatureIdentifier.substring(0, listOfFeatureIdentifier.length() - 1));
		protocolSheet.write(protocol);
	}

	private void collectVariableInfo(ExcelReader reader) throws IOException
	{
		ExcelSheetReader variableSheet = reader.getSheet(0);
		Iterator<?> colNamesIterator = variableSheet.colNamesIterator();
		List<String> colNamesList = new ArrayList<String>();
		while (colNamesIterator.hasNext())
		{
			colNamesList.add(colNamesIterator.next().toString());
		}

		Iterator<Tuple> rowTuples = variableSheet.iterator();
		while (rowTuples.hasNext())
		{
			Tuple eachRow = rowTuples.next();
			String variableID = eachRow.getString("name");
			String label = eachRow.getString("label:en");
			String dataType = eachRow.getString("valueType");
			// String variableID = eachRow.getString("ID");
			// String variableName = eachRow.getString("Name");
			// String label = eachRow.getString("Text");

			if (variableID != null) variableID = variableID.trim();
			if (label != null) label = label.trim();
			// if (label != null) label = label.trim();
			// if (dataType != null) dataType = dataType.trim();

			if (!variableInfo.containsKey(variableID))
			{
				UniqueVariable newVariable = new UniqueVariable(variableID, label, dataType);
				variableInfo.put(variableID, newVariable);
			}
		}
	}

	private void collectCategoryInfo(ExcelReader reader) throws IOException
	{
		ExcelSheetReader categoryReader = reader.getSheet(1);
		Iterator<?> iterator = categoryReader.colNamesIterator();
		List<String> listOfColumnHeaders = new ArrayList<String>();
		while (iterator.hasNext())
		{
			listOfColumnHeaders.add(iterator.next().toString());
		}
		Iterator<Tuple> listOfRows = categoryReader.iterator();
		while (listOfRows.hasNext())
		{
			Tuple eachRow = listOfRows.next();
			String featureID = eachRow.getString("variable");
			// String featureName = eachRow.getString("variable");
			if (featureID != null)
			{
				featureID = featureID.trim();

				String code = eachRow.getString("name");
				String categoryDescription = eachRow.getString("label:en");

				// String code = eachRow.getString("Value");
				// String categoryDescription = eachRow.getString("Text");

				List<UniqueCategory> listOfCategoriesPerVariable = null;
				if (categoryInfo.containsKey(featureID))
				{
					listOfCategoriesPerVariable = categoryInfo.get(featureID);
				}
				else
				{
					listOfCategoriesPerVariable = new ArrayList<UniqueCategory>();
				}
				listOfCategoriesPerVariable.add(new UniqueCategory(studyName + "_" + featureID + "_" + code, code,
						categoryDescription));
				categoryInfo.put(featureID, listOfCategoriesPerVariable);
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		new OpalToOMXConvertor("Healthy Obese Project",
				"/Users/chaopang/Documents/Chao_Work/HarmonizationTestDataset/converted-OMX/HOP-dictionary.xlsx");
	}

	class UniqueVariable
	{
		private final String variable;
		private final String label;
		private final String dataType;

		public UniqueVariable(String variable, String label, String dataType)
		{
			this.variable = variable;
			this.label = label;
			this.dataType = dataType;
		}

		public String getVariable()
		{
			return variable;
		}

		public String getLabel()
		{
			return label;
		}

		public String getDataType()
		{
			return dataType;
		}
	}

	class UniqueCategory
	{
		private final String code;
		private final String label;
		private final String name;

		public UniqueCategory(String code, String label)
		{
			this.code = code.trim();
			if (label != null) this.label = label.trim();
			else this.label = StringUtils.EMPTY;
			StringBuilder name = new StringBuilder();
			this.name = name.append(studyName).append('_').append(code).append('_').append(label).toString()
					.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
		}

		public UniqueCategory(String name, String code, String label)
		{
			this.code = code.trim();
			if (label != null) this.label = label.trim();
			else this.label = StringUtils.EMPTY;
			this.name = name.replaceAll("[^a-zA-Z0-9_]", "_");
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			UniqueCategory other = (UniqueCategory) obj;
			if (!getOuterType().equals(other.getOuterType())) return false;
			if (name == null)
			{
				if (other.name != null) return false;
			}
			else if (!name.equals(other.name)) return false;
			return true;
		}

		public String getName()
		{
			return name;
		}

		public String getCode()
		{
			return code;
		}

		public String getLabel()
		{
			return label;
		}

		private OpalToOMXConvertor getOuterType()
		{
			return OpalToOMXConvertor.this;
		}
	}
}