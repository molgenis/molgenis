package org.molgenis.tools.compare;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntitySource;
import org.molgenis.data.Repository;
import org.molgenis.data.excel.ExcelEntitySourceFactory;

public class PalgaConverter
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1)
		{
			System.err.println("1 argument please 1) filename \n"
					+ "example:\n/Users/Roan/Work/GIDS_Imported_30_august_2013/Omx_Import_CeliacSprue_metadata.xls");

			return;
		}
		PalgaConverter vc = new PalgaConverter();
		// vc.convertTo256Characters(args[0]);
		vc.check(args[0]);
	}

	public void convertTo256Characters(String file) throws IOException
	{
		EntitySource entitySource = new ExcelEntitySourceFactory().create(new File(file));
		Repository repo = entitySource.getRepositoryByEntityName("ontologyterm");

		for (Entity entity : repo)
		{
			String name = entity.getString("name");
			if (name.length() > 256)
			{

				System.out.println(name.substring(0, 250) + "...");
			}
			else
			{
				System.out.println(name);
			}
		}
		entitySource.close();
	}

	public void check(String file) throws IOException
	{
		EntitySource entitySource = new ExcelEntitySourceFactory().create(new File(file));
		Repository repo = entitySource.getRepositoryByEntityName("dataset_palga");
		List<String> list = null;
		List<List<String>> listOfLists = new ArrayList<List<String>>();
		for (Entity entity : repo)
		{
			list = new ArrayList<String>();
			String[] code = entity.getString("PALGA-code").split(",");
			for (int i = 0; i < code.length; ++i)
			{
				if (!list.contains(code[i]))
				{
					list.add(code[i]);
				}
			}
			listOfLists.add(list);
		}
		for (List<String> all : listOfLists)
		{
			System.out.println(all);

		}

		entitySource.close();
	}
}
