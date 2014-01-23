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
import org.molgenis.data.Entity;
import org.molgenis.data.EntitySource;
import org.molgenis.data.Writable;
import org.molgenis.data.WritableFactory;
import org.molgenis.data.excel.ExcelEntitySourceFactory;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.support.MapEntity;

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
			EntitySource entitySource = null;
			ExcelWriter<Entity> writer = null;
			try
			{
				entitySource = new ExcelEntitySourceFactory().create(file);

				// Handle category sheet first
				collectCategoryInfo(entitySource);
				// Handle variable sheet second
				collectVariableInfo(entitySource);
				// Handle protocol sheet second
				collectProtocolInfo(entitySource);
			}
			finally
			{
				if (entitySource != null) entitySource.close();
			}

			try
			{
				writer = new ExcelWriter<Entity>(new File(file.getAbsolutePath() + "_OMX.xls"));
				writeToPhenoFormat(writer);
			}
			finally
			{
				writer.close();
			}
		}
	}

	public void writeToPhenoFormat(WritableFactory<Entity> writer) throws IOException
	{
		String protocolIdentifier = studyName + "_protocol";
		StringBuilder listOfFeatureIdentifier = new StringBuilder();
		// Create sheet for investigation
		Writable dataSet = null;
		try
		{
			dataSet = writer.createWritable("dataset", Arrays.asList("identifier", "name", "protocolUsed_identifier"));
			Entity entity = new MapEntity();
			entity.set("identifier", studyName);
			entity.set("name", studyName);
			entity.set("protocolUsed_identifier", protocolIdentifier);
			dataSet.add(entity);
		}
		finally
		{
			if (dataSet != null) dataSet.close();
		}
		// Create sheet for category
		Writable categorySheet = null;

		try
		{
			categorySheet = writer.createWritable("category",
					Arrays.asList("identifier", "name", "valueCode", "observablefeature_identifier"));

			for (Entry<String, List<UniqueCategory>> entry : featureCategoryLinks.entrySet())
			{
				String featureIdentifier = createFeatureIdentifier(entry.getKey());
				List<UniqueCategory> categoriesPerFeature = entry.getValue();
				for (UniqueCategory eachCategory : categoriesPerFeature)
				{
					Entity entity = new MapEntity();
					entity.set("identifier", eachCategory.getIdentifier());
					entity.set("name", eachCategory.getLabel());
					entity.set("valueCode", eachCategory.getCode());
					entity.set("observablefeature_identifier", featureIdentifier);
					categorySheet.add(entity);
				}
			}
		}
		finally
		{
			if (categorySheet != null) categorySheet.close();
		}

		// Create sheet for variable
		Writable featureWriter = null;
		try
		{
			featureWriter = writer.createWritable("observablefeature",
					Arrays.asList("identifier", "name", "description", "dataType", "unit_Identifier"));
			int count = 0;
			for (UniqueVariable eachVariable : variableInfo.values())
			{
				Entity eachRow = new MapEntity();
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

				listOfFeatureIdentifier.append(identifier).append(',');
				eachRow.set("identifier", identifier);
				eachRow.set("name", name);
				eachRow.set("description", eachVariable.getLabel());
				eachRow.set("dataType", dataType);
				featureWriter.add(eachRow);
				System.out.println(++count);
			}
		}
		finally
		{
			if (featureWriter != null) featureWriter.close();
		}

		// Create sheet for protocol
		Writable protocolSheet = null;
		try
		{
			protocolSheet = writer.createWritable("protocol",
					Arrays.asList("identifier", "name", "features_identifier", "subprotocols_identifier"));

			Entity protocol = new MapEntity();
			protocol.set("identifier", protocolIdentifier);
			protocol.set("name", protocolIdentifier);
			List<String> subProtocolIdentifiers = new ArrayList<String>();
			if (protocolFeatureLinks.size() == 0)
			{
				protocol.set("features_identifier",
						listOfFeatureIdentifier.substring(0, listOfFeatureIdentifier.length() - 1).toString());
				System.out.println(listOfFeatureIdentifier.substring(0, listOfFeatureIdentifier.length() - 1)
						.toString());
			}
			else
			{
				for (Entry<String, List<String>> entry : protocolFeatureLinks.entrySet())
				{
					String subProtocolName = entry.getKey();
					String subProtocolIdentifier = createProtocolIdentifier(subProtocolName);
					Entity subProtocol = new MapEntity();
					subProtocol.set("identifier", subProtocolIdentifier);
					subProtocol.set("name", subProtocolName);
					subProtocol.set("features_identifier", StringUtils.join(entry.getValue(), ','));
					protocolSheet.add(subProtocol);

					subProtocolIdentifiers.add(subProtocolIdentifier);
				}
			}
			protocol.set("subprotocols_identifier", StringUtils.join(subProtocolIdentifiers, ','));
			protocolSheet.add(protocol);
		}
		finally
		{
			if (protocolSheet != null) protocolSheet.close();
		}
	}

	public abstract void collectProtocolInfo(EntitySource entitySource) throws IOException;

	public abstract void collectVariableInfo(EntitySource entitySource) throws IOException;

	public abstract void collectCategoryInfo(EntitySource entitySource) throws IOException;

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