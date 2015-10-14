package org.molgenis.tools.compare;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.processor.TrimProcessor;

public class CategoryParser
{

	public static void main(String[] args) throws IOException, MolgenisInvalidFormatException
	{
		if (args.length != 2)
		{
			System.err.println("2 arguments please 1) filename 2) name of datamatrix tab\n"
					+ "example:\n/Users/Roan/Work/GIDS_Imported_30_august_2013/Omx_Import_CeliacSprue_metadata.xls"
					+ "dataset_celiac_sprue");
			return;
		}
		CategoryParser vc = new CategoryParser();
		vc.check(args[0], args[1]);
	}

	public void check(String file, String datasetMatrix) throws IOException, MolgenisInvalidFormatException
	{
		RepositoryCollection repositorySource = new ExcelRepositoryCollection(new File(file), new TrimProcessor());

		List<String> listOfCategoricalFeatures = new ArrayList<String>();
		Map<String, List<String>> hashCategories = new HashMap<String, List<String>>();

		Repository repo = repositorySource.getRepository("observablefeature");
		try
		{
			for (Entity entity : repo)
			{
				if ("categorical".equals(entity.getString("datatype")))
				{
					listOfCategoricalFeatures.add(entity.getString("identifier"));
					hashCategories.put(entity.getString("identifier"), new ArrayList<String>());
				}
			}
		}
		finally
		{
			repo.close();
		}

		Repository readObservableDataMatrixRepo = repositorySource.getRepository(datasetMatrix);
		try
		{
			for (Entity entity : readObservableDataMatrixRepo)
			{
				for (String category : listOfCategoricalFeatures)
				{
					List<String> getList = hashCategories.get(category);
					if (!hashCategories.get(category).contains(entity.getString(category)))
					{
						getList.add(entity.getString(category));
					}
				}
			}
			printForCategoryTab(hashCategories);
		}
		finally
		{
			readObservableDataMatrixRepo.close();
		}

	}

	public void printAsList(Map<String, List<String>> hashCategories)
	{
		for (Entry<String, List<String>> entry : hashCategories.entrySet())
		{
			System.out.println(entry.getKey() + "-" + entry.getValue());
		}
	}

	public void printForCategoryTab(Map<String, List<String>> hashCategories)
	{
		System.out.println("identifier\tname\tvalueCode\tobservablefeature_identifier");
		for (Entry<String, List<String>> entry : hashCategories.entrySet())
		{
			for (String valueCode : entry.getValue())
			{
				if (StringUtils.isNotBlank(valueCode))
				{
					String identifierCode = valueCode.replaceAll("\\s", "_");
					System.out.println(entry.getKey() + "_" + identifierCode + "\t" + valueCode + "\t" + valueCode
							+ "\t" + entry.getKey());
				}
			}
		}
	}
}
