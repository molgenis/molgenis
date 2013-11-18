package org.molgenis.omx.biobankconnect.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.util.tuple.Tuple;

public class PhenoToOmxConvertor extends AbstractOmxConvertor
{
	public PhenoToOmxConvertor(String studyName, String filePath) throws IOException
	{
		super(studyName, filePath);
	}

	@Override
	public void collectProtocolInfo(ExcelReader reader) throws IOException
	{
		if (reader.getSheet("Protocol") != null)
		{
			ExcelSheetReader measurementReader = reader.getSheet("Protocol");
			Iterator<Tuple> rows = measurementReader.iterator();
			while (rows.hasNext())
			{
				Tuple tuple = rows.next();
				String name = tuple.getString("name");
				if (!protocolFeatureLinks.containsKey(name))
				{
					protocolFeatureLinks.put(name, new ArrayList<String>());
				}
				if (!tuple.getString("Features_name").isEmpty())
				{
					for (String featureName : tuple.getString("Features_name").split(","))
					{
						protocolFeatureLinks.get(name).add(createFeatureIdentifier(featureName));
					}
				}
			}
		}
	}

	@Override
	public void collectVariableInfo(ExcelReader reader) throws IOException
	{
		ExcelSheetReader measurementReader = reader.getSheet("Measurement");
		Iterator<Tuple> rows = measurementReader.iterator();
		while (rows.hasNext())
		{
			Tuple tuple = rows.next();
			String featureName = tuple.getString("name");
			String description = tuple.getString("description");
			String dataType = "string";
			if (!tuple.getString("categories_name").isEmpty())
			{
				dataType = "categorical";

				if (!featureCategoryLinks.containsKey(featureName)) featureCategoryLinks.put(featureName,
						new ArrayList<UniqueCategory>());
				List<UniqueCategory> listOfCategories = featureCategoryLinks.get(featureName);
				for (String categoryName : tuple.getString("categories_name").split(","))
				{
					UniqueCategory category = copyCategoryContent(categoryInfo.get(categoryName));
					category.setIdentifier(createCategoryIdentifier(featureName + "_" + category.getCode()));
					if (!listOfCategories.contains(category)) listOfCategories.add(category);
				}
				featureCategoryLinks.put(featureName, listOfCategories);
			}
			if (!variableInfo.containsKey(featureName))
			{
				UniqueVariable newVariable = new UniqueVariable(featureName, description, dataType);
				variableInfo.put(featureName, newVariable);
			}
		}
	}

	@Override
	public void collectCategoryInfo(ExcelReader reader) throws IOException
	{
		ExcelSheetReader measurementReader = reader.getSheet("Category");
		Iterator<Tuple> rows = measurementReader.iterator();
		while (rows.hasNext())
		{
			Tuple tuple = rows.next();
			String name = tuple.getString("name");
			String description = tuple.getString("description");
			String code = tuple.getString("code_string");
			categoryInfo.put(name, new UniqueCategory(name, code, description));
		}
	}

	public UniqueCategory copyCategoryContent(UniqueCategory category)
	{
		return new UniqueCategory(category.getName(), category.getCode(), category.getLabel());
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		new PhenoToOmxConvertor(args[0], args[1]);
	}
}