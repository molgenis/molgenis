package org.molgenis.tools.compare;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositorySource;
import org.molgenis.data.excel.ExcelRepositorySource;
import org.molgenis.data.processor.TrimProcessor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * In this class are several handy tools e.g. * make big file * convert to 256 chars * Producing a BCrypt password
 * 
 * @author Roan
 * 
 */

public class ParsingToolKit
{
	// Choose which tool you want to run
	public static void main(String[] args) throws IOException
	{
		if (args.length != 1)
		{
			System.err.println("1 argument please 1) filename \n"
					+ "example:\n/Users/Roan/Work/GIDS_Imported_30_august_2013/Omx_Import_CeliacSprue_metadata.xls");

			return;
		}
		ParsingToolKit vc = new ParsingToolKit();
		vc.makeBigFile();
		// vc.convertTo256Characters(args[0]);
		// vc.check(args[0]);

	}

	// Convert a value to max 256 characters
	public void convertTo256Characters(String file) throws IOException, InvalidFormatException
	{
		RepositorySource repositorySource = new ExcelRepositorySource(new File(file), new TrimProcessor());
		Repository repo = repositorySource.getRepository("ontologyterm");

		for (Entity entity : repo)
		{
			String name = entity.getString("name");
			if (name.length() >= 256)
			{

				System.out.println(name.substring(0, 252) + "...");
			}
			else
			{
				System.out.println(name);
			}
		}
	}

	// Parse your password
	public void password()
	{
		String stringToParse = "palgaMolgenis";
		System.out.println(new BCryptPasswordEncoder().encode(stringToParse));
	}

	// Creating randomly some columns for making a big dataset
	public void makeBigFile()
	{

		String[] list =
		{ "0-5", "6-15", "16-25", "26-35", "36-45", "46-55", "56-65", "66-75", "76-85", "86-95", ">96" };

		int listSize = list.length - 1;

		int bool = 0;
		int family = 1;
		for (int i = 1; i <= 100000; ++i)
		{
			int math = 0 + (int) (Math.random() * ((listSize - 0) + 1));
			boolean printBool = false;
			double number = Math.random() * 100;
			double number2 = Math.random() * 100;
			int total = (int) (number * number2);
			if (bool % 3 == 0)
			{
				printBool = true;
			}
			if (i % 4 == 0)
			{
				family++;
			}

			System.out.println("Patient_" + i + "\t" + bool + "\t" + number + "\t" + total + "\t" + printBool + "\t"
					+ "Family" + family + "\t" + list[math]);
			bool++;
		}

	}

	// Palga project specific
	// This code reads an excelfile and produces a list with in the first column the Palga-code and the second
	// column a list of all the referring terms
	public void check(String file) throws IOException, InvalidFormatException
	{
		RepositorySource repositorySource = new ExcelRepositorySource(new File(file), new TrimProcessor());
		Repository repo = repositorySource.getRepository("dataset_palga");
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
	}
}
