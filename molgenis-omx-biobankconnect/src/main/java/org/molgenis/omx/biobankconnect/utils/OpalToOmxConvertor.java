package org.molgenis.omx.biobankconnect.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntitySource;
import org.molgenis.data.Repository;

public class OpalToOmxConvertor extends AbstractOmxConvertor
{
	public OpalToOmxConvertor(String studyName, String filePath) throws IOException
	{
		super(studyName, filePath);
	}

	@Override
	public void collectProtocolInfo(EntitySource entitySource) throws IOException
	{
		System.out.println("No protocol involved in OPAL format!");
	}

	@Override
	public void collectVariableInfo(EntitySource entitySource) throws IOException
	{
		Repository repo = entitySource.getRepositoryByEntityName("Variables");
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
	public void collectCategoryInfo(EntitySource entitySource) throws IOException
	{
		Repository repo = entitySource.getRepositoryByEntityName("Categories");
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
	 */
	public static void main(String[] args) throws IOException
	{
		new OpalToOmxConvertor(args[0], args[1]);
	}
}