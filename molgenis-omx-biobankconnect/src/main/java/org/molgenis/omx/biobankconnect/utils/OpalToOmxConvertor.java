package org.molgenis.omx.biobankconnect.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositorySource;

public class OpalToOmxConvertor extends AbstractOmxConvertor
{
	public OpalToOmxConvertor(String studyName, String filePath) throws IOException, InvalidFormatException
	{
		super(studyName, filePath);
	}

	@Override
	public void collectProtocolInfo(RepositorySource repositorySource) throws IOException
	{
		System.out.println("No protocol involved in OPAL format!");
	}

	@Override
	public void collectVariableInfo(RepositorySource repositorySource) throws IOException
	{
		Repository repo = repositorySource.getRepository("Variables");
		for (Entity entity : repo)
		{
			String variableName = entity.getString("name");
			String label = entity.getString("label:en");
			String dataType = entity.getString("valueType");

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

	@Override
	public void collectCategoryInfo(RepositorySource repositorySource) throws IOException
	{
		Repository repo = repositorySource.getRepository("Categories");
		for (Entity entity : repo)
		{
			String featureID = entity.getString("variable");
			if (featureID != null)
			{
				featureID = featureID.trim();

				String code = entity.getString("name");
				String categoryDescription = entity.getString("label:en");

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
	 * @throws InvalidFormatException
	 */
	public static void main(String[] args) throws IOException, InvalidFormatException
	{
		new OpalToOmxConvertor(args[0], args[1]);
	}
}