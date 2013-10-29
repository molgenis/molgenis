package org.molgenis.omx.biobankconnect.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetWriter;
import org.molgenis.io.excel.ExcelWriter;
import org.molgenis.util.tuple.KeyValueTuple;

abstract class AbstractOmxConvertor
{
	public Map<String, List<UniqueCategory>> featureCategoryLinks = new LinkedHashMap<String, List<UniqueCategory>>();
	public Map<String, List<String>> protocolFeatureLinks = new LinkedHashMap<String, List<String>>();
	public Map<String, UniqueVariable> variableInfo = new LinkedHashMap<String, UniqueVariable>();
	public Map<String, UniqueCategory> categoryInfo = new LinkedHashMap<String, UniqueCategory>();

	public String studyName = null;

	public AbstractOmxConvertor(String studyName, String filePath) throws IOException
	{
		this.studyName = studyName;
		start(filePath);
	}

	public void start(String fileString) throws IOException
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
				// Handle protocol sheet second
				collectProtocolInfo(reader);
			}
			finally
			{
				if (reader != null) reader.close();
			}

			ExcelWriter writer = null;
			try
			{
				writer = new ExcelWriter(new File(file.getAbsolutePath() + "_OMX.xls"));
				writeToPhenoFormat(writer);
			}
			finally
			{
				writer.close();
			}
		}
	}

	public void writeToPhenoFormat(ExcelWriter writer) throws IOException
	{
		String protocolIdentifier = studyName + "_protocol";
		String listOfFeatureIdentifier = "";
		// Create sheet for investigation
		ExcelSheetWriter dataSet = null;
		// CSVWriter dataSet = null;
		try
		{
			dataSet = (ExcelSheetWriter) writer.createTupleWriter("dataset");
			dataSet.writeColNames(Arrays.asList("identifier", "name", "protocolUsed_identifier"));
			KeyValueTuple row = new KeyValueTuple();
			row.set("identifier", studyName);
			row.set("name", studyName);
			row.set("protocolUsed_identifier", protocolIdentifier);
			dataSet.write(row);
		}
		finally
		{
			if (dataSet != null) dataSet.close();
		}
		// Create sheet for category
		ExcelSheetWriter categorySheet = null;

		try
		{
			categorySheet = (ExcelSheetWriter) writer.createTupleWriter("category");
			categorySheet.writeColNames(Arrays
					.asList("identifier", "name", "valueCode", "observablefeature_identifier"));

			for (Entry<String, List<UniqueCategory>> entry : featureCategoryLinks.entrySet())
			{
				String featureIdentifier = createFeatureIdentifier(entry.getKey());
				List<UniqueCategory> categoriesPerFeature = entry.getValue();
				for (UniqueCategory eachCategory : categoriesPerFeature)
				{
					KeyValueTuple eachRow = new KeyValueTuple();
					eachRow.set("identifier", eachCategory.getIdentifier());
					eachRow.set("name", eachCategory.getLabel());
					eachRow.set("valueCode", eachCategory.getCode());
					eachRow.set("observablefeature_identifier", featureIdentifier);
					categorySheet.write(eachRow);
				}
			}
		}
		finally
		{
			if (categorySheet != null) categorySheet.close();
		}

		// Create sheet for variable
		ExcelSheetWriter featureWriter = null;
		try
		{
			featureWriter = (ExcelSheetWriter) writer.createTupleWriter("observablefeature");
			featureWriter.writeColNames(Arrays.asList("identifier", "name", "description", "dataType",
					"unit_Identifier"));
			int count = 0;
			for (UniqueVariable eachVariable : variableInfo.values())
			{
				KeyValueTuple eachRow = new KeyValueTuple();
				String name = eachVariable.getVariable();
				String dataType = eachVariable.getDataType();
				String identifier = eachVariable.getIdentifier();
				StringBuilder categoriesName = new StringBuilder();
				if (featureCategoryLinks.containsKey(name))
				{
					for (UniqueCategory eachCategory : featureCategoryLinks.get(name))
					{
						categoriesName.append(eachCategory.getName()).append(',');
					}
					dataType = "categorical";
				}
				else if (dataType != null && dataType.equals("integer")) dataType = "int";
				else if (dataType != null && dataType.equals("decimal")) dataType = "decimal";
				else dataType = "string";

				listOfFeatureIdentifier += identifier + ",";
				eachRow.set("identifier", identifier);
				eachRow.set("name", name);
				eachRow.set("description", eachVariable.getLabel());
				eachRow.set("dataType", dataType);
				featureWriter.write(eachRow);
				System.out.println(++count);
			}
		}
		finally
		{
			if (featureWriter != null) featureWriter.close();
		}

		// Create sheet for protocol
		ExcelSheetWriter protocolSheet = null;
		try
		{
			protocolSheet = (ExcelSheetWriter) writer.createTupleWriter("protocol");
			protocolSheet.writeColNames(Arrays.asList("identifier", "name", "features_identifier",
					"subprotocols_identifier"));
			KeyValueTuple protocol = new KeyValueTuple();
			protocol.set("identifier", protocolIdentifier);
			protocol.set("name", protocolIdentifier);
			List<String> subProtocolIdentifiers = new ArrayList<String>();
			if (protocolFeatureLinks.size() == 0)
			{
				protocol.set("features_identifier",
						listOfFeatureIdentifier.substring(0, listOfFeatureIdentifier.length() - 1));
				System.out.println(listOfFeatureIdentifier.substring(0, listOfFeatureIdentifier.length() - 1));
			}
			else
			{
				for (Entry<String, List<String>> entry : protocolFeatureLinks.entrySet())
				{
					String subProtocolName = entry.getKey();
					String subProtocolIdentifier = createProtocolIdentifier(subProtocolName);
					KeyValueTuple subProtocol = new KeyValueTuple();
					subProtocol.set("identifier", subProtocolIdentifier);
					subProtocol.set("name", subProtocolName);
					subProtocol.set("features_identifier", StringUtils.join(entry.getValue(), ','));
					protocolSheet.write(subProtocol);

					subProtocolIdentifiers.add(subProtocolIdentifier);
				}
			}
			protocol.set("subprotocols_identifier", StringUtils.join(subProtocolIdentifiers, ','));
			protocolSheet.write(protocol);
		}
		finally
		{
			if (protocolSheet != null) protocolSheet.close();
		}
	}

	public abstract void collectProtocolInfo(ExcelReader reader) throws IOException;

	public abstract void collectVariableInfo(ExcelReader reader) throws IOException;

	public abstract void collectCategoryInfo(ExcelReader reader) throws IOException;

	public String createFeatureIdentifier(String featureName)
	{
		return studyName + "_feature_" + featureName;
	}

	public String createProtocolIdentifier(String protocolTname)
	{
		return studyName + "_protocol_" + protocolTname;
	}

	public String createCategoryIdentifier(String categoryName)
	{
		return studyName + "_category_" + categoryName;
	}

	public class UniqueVariable
	{
		private String identifier;
		private final String variable;
		private final String label;
		private final String dataType;

		public UniqueVariable(String variable, String label, String dataType)
		{
			this.variable = variable;
			this.label = label;
			this.dataType = dataType;
			this.identifier = createFeatureIdentifier(variable);
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

		public String getIdentifier()
		{
			return identifier;
		}

		public void setIdentifier(String identifier)
		{
			this.identifier = identifier;
		}
	}

	public class UniqueCategory
	{
		private final String code;
		private final String label;
		private final String name;
		private String identifier;

		public UniqueCategory(String code, String label)
		{
			this.code = code.trim();
			if (label != null) this.label = label.trim();
			else this.label = StringUtils.EMPTY;
			StringBuilder name = new StringBuilder();
			this.name = name.append(code).append('_').append(label).toString().replaceAll("[^a-zA-Z0-9_]", "_")
					.toLowerCase();
			this.identifier = createCategoryIdentifier(this.name);
		}

		public UniqueCategory(String name, String code, String label)
		{
			this.code = code.trim();
			if (label != null) this.label = label.trim();
			else this.label = StringUtils.EMPTY;
			this.name = name.replaceAll("[^a-zA-Z0-9_]", "_");
			this.identifier = createCategoryIdentifier(this.name);
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

		public String getIdentifier()
		{
			return identifier;
		}

		public void setIdentifier(String identifier)
		{
			this.identifier = identifier;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((code == null) ? 0 : code.hashCode());
			result = prime * result + ((label == null) ? 0 : label.hashCode());
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
			if (code == null)
			{
				if (other.code != null) return false;
			}
			else if (!code.equals(other.code)) return false;
			if (label == null)
			{
				if (other.label != null) return false;
			}
			else if (!label.equals(other.label)) return false;
			return true;
		}

		private AbstractOmxConvertor getOuterType()
		{
			return AbstractOmxConvertor.this;
		}
	}
}