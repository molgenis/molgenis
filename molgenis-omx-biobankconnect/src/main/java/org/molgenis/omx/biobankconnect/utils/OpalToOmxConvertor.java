package org.molgenis.omx.biobankconnect.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.util.tuple.Tuple;

public class OpalToOmxConvertor extends AbstractOmxConvertor
{
	public OpalToOmxConvertor(String studyName, String filePath) throws IOException
	{
		super(studyName, filePath);
	}

	@Override
	public void collectProtocolInfo(ExcelReader reader) throws IOException
	{
		System.out.println("No protocol involved in OPAL format!");
	}

	public void collectVariableInfo(ExcelReader reader) throws IOException
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
			String variableName = eachRow.getString("name");
			String label = eachRow.getString("label:en");
			String dataType = eachRow.getString("valueType");

			if (variableName != null) variableName = variableName.trim();
			if (label != null) label = label.trim();

			if (!variableInfo.containsKey(variableName))
			{
				UniqueVariable newVariable = new UniqueVariable(variableName, label, dataType);
				newVariable.setIdentifier(createFeatureIdentifier(variableName));
				variableInfo.put(variableName, newVariable);
			}
		}
	}

	public void collectCategoryInfo(ExcelReader reader) throws IOException
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
			if (featureID != null)
			{
				featureID = featureID.trim();

				String code = eachRow.getString("name");
				String categoryDescription = eachRow.getString("label:en");

				List<UniqueCategory> listOfCategoriesPerVariable = null;
				if (featureCategoryLinks.containsKey(featureID))
				{
					listOfCategoriesPerVariable = featureCategoryLinks.get(featureID);
				}
				else
				{
					listOfCategoriesPerVariable = new ArrayList<UniqueCategory>();
				}
				listOfCategoriesPerVariable.add(new UniqueCategory(featureID + "_" + code, code, categoryDescription));
				featureCategoryLinks.put(featureID, listOfCategoriesPerVariable);
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		new OpalToOmxConvertor(args[0], args[1]);
	}
}