package org.molgenis.gids.tools.compare;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.util.tuple.Tuple;

public class CategoryChecker
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 2)
		{
			System.err.println("2 arguments please 1) filename 2) name of datamatrix tabs");
			return;
		}
		CategoryChecker vc = new CategoryChecker();

		vc.check(args[0], args[1]);

	}

	public void check(String file, String datasetMatrix) throws IOException
	{
		ExcelReader excelReader = new ExcelReader(new File(file));
		List<String> listOfCategoricalFeatures = new ArrayList<String>();
		ExcelSheetReader readObservableFeatureSheet = excelReader.getSheet("observablefeature");

		Map<String, List<String>> hashCategories = new HashMap<String, List<String>>();
		for (Tuple t : readObservableFeatureSheet)
		{
			if (StringUtils.isNotBlank(t.getString("dataType")))
			{
				if (t.getString("dataType").equals("categorical"))
				{
					listOfCategoricalFeatures.add(t.getString("identifier"));
					hashCategories.put(t.getString("identifier"), new ArrayList<String>());
				}
			}
		}

		ExcelSheetReader readObservableDataMatrixSheet = excelReader.getSheet(datasetMatrix);

		for (Tuple t : readObservableDataMatrixSheet)
		{
			for (String category : listOfCategoricalFeatures)
			{
				List<String> getList = hashCategories.get(category);
				if (!hashCategories.get(category).contains(t.getString(category)))
				{
					getList.add(t.getString(category));

				}

			}
		}
		printForCategoryTab(hashCategories);

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
		for (Entry<String, List<String>> entry : hashCategories.entrySet())
		{

			for (String valueCode : entry.getValue())
			{

				System.out.println(entry.getKey() + "_" + valueCode + "," + valueCode + "," + valueCode + ","
						+ entry.getKey());
			}
		}
	}

}
