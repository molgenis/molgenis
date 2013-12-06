package org.molgenis.gids.tools.compare;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.util.tuple.Tuple;

/**
 * This script is created to compare 2 excelfiles. The files are given via the arguments
 */

public class Validator
{
	private final static String IDENTIFIER = "id_sample";
	private final static String IDENTIFIER2 = "id_individual";

	public void check(String excelFile1, String file2, BufferedWriter logger) throws IOException
	{
		ValidationFile excelfile = new ValidationFile();
		ExcelReader excelReaderReferenceFile = new ExcelReader(new File(excelFile1));

		ExcelSheetReader excelSheetReaderReferenceFile = excelReaderReferenceFile.getSheet("dataset_celiac_sprue");
		// ExcelSheetReader excelSheetReaderReferenceFile = excelReaderReferenceFile.getSheet("Sheet1");
		excelfile.readFile(excelSheetReaderReferenceFile, IDENTIFIER, IDENTIFIER2);

		ValidationFile csvFile = new ValidationFile();
		CsvReader csvReaderFileToCompare = new CsvReader(new File(file2));

		csvFile.readFile(csvReaderFileToCompare, IDENTIFIER, IDENTIFIER2);
		boolean noUniqueColums = false;
		List<String> listOfSharedHeaders = new ArrayList<String>();

		System.out.println("-----------------------------------------------------------------------------------");
		System.out.println("### Unique columns");
		for (String o : excelfile.getListOfHeaders())
		{
			listOfSharedHeaders.add(o);

			if (!csvFile.getListOfHeaders().contains(o))
			{
				listOfSharedHeaders.remove(o);
				System.out.println("In file1: " + o);
				noUniqueColums = true;
			}
		}

		for (String o : csvFile.getListOfHeaders())
		{
			if (!excelfile.getListOfHeaders().contains(o))
			{
				System.out.println("In file2: " + o);
				noUniqueColums = true;
			}
		}

		if (noUniqueColums == false)
		{

			System.out.println("###There are no added/deleted columns\n");
		}
		System.out.println("-----------------------------------------------------------------------------------");
		System.out.println("\n###Comparing the values ");
		System.out.println("Sample ID\tFeature\tFile1\tFile2");

		try
		{
			for (Entry<String, Tuple> entry : excelfile.getHash().entrySet())
			{
				compareRows(
						"excel",
						"csvFile",
						entry.getValue(),
						csvFile.getHash().get(
								entry.getValue().getString(IDENTIFIER) + "_" + entry.getValue().getString(IDENTIFIER2)),
						listOfSharedHeaders, logger);
			}

			for (Entry<String, Tuple> entry : csvFile.getHash().entrySet())
			{
				compareRows(
						"csvFile",
						"excel",
						entry.getValue(),
						excelfile.getHash().get(
								entry.getValue().getString(IDENTIFIER) + "_" + entry.getValue().getString(IDENTIFIER2)),
						listOfSharedHeaders, logger);
			}
			System.out.println("-----------------------------------------------------------------------------------");
			System.out.println("\n###Unique samples in file1");
			if (excelfile.getHash().size() == 0)
			{
				System.out.println("There are no unique samples in file1");
			}
			for (Entry<String, Tuple> entry : excelfile.getHash().entrySet())
			{
				if (!csvFile.getHash().containsKey(entry.getKey()))
				{
					System.out.println(entry.getKey());
				}
			}
			System.out.println("-----------------------------------------------------------------------------------");
			System.out.println("\n###Unique samples in file2 ");
			if (csvFile.getHash().size() == 0)
			{
				System.out.println("There are no unique samples in file2");
			}
			for (Entry<String, Tuple> entry : csvFile.getHash().entrySet())
			{
				if (!excelfile.getHash().containsKey(entry.getKey()))
				{
					System.out.println(entry.getKey());
				}
			}

		}
		finally
		{
			IOUtils.closeQuietly(csvReaderFileToCompare);
			IOUtils.closeQuietly(excelReaderReferenceFile);
		}
	}

	public static void compareRows(String fileName1, String fileName2, Tuple firstTuple, Tuple secondTuple,
			List<String> listOfHeaders, BufferedWriter logger) throws IOException
	{

		for (String e : listOfHeaders)
		{
			if (firstTuple == null || secondTuple == null)
			{
				System.out.println("NULLLLL");
			}
			if (firstTuple.getString(e) == null && secondTuple.getString(e) == null)
			{
				// Do nothing
			}
			else if (firstTuple.getString(e) != null && secondTuple.getString(e) == null)
			{

				System.out.println(fileName1 + " ### " + firstTuple.getString(IDENTIFIER) + "\t" + e + "\t"
						+ "REMOVED IN:" + fileName2 + "\t" + firstTuple.getString(e));
			}
			else if (firstTuple.getString(e) == null && secondTuple.getString(e) != null)
			{
				System.out.println(fileName1 + "---" + firstTuple.getString(IDENTIFIER) + "\t" + e + "\t"
						+ "\tADDED IN " + fileName2 + "|\t|" + firstTuple.getString(e) + "|");
			}
			else
			{
				if (!firstTuple.getString(e).equals(secondTuple.getString(e)))
				{
					System.out.println(fileName1 + "-" + firstTuple.getString(IDENTIFIER) + "\t" + e + "\t"
							+ secondTuple.getString(e) + "|\t|" + firstTuple.getString(e) + "|");
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		if (args.length != 3)
		{
			System.err
					.println("To run this script it needs 3 arguments; \n1) An excelfile\n2) a csvfile\n"
							+ "3) a path for the logfile\n"
							+ "e.g.\n"
							+ "args[0] /Users/Roan/Work/GIDS_8_may/Cohorts/Bloodbank/Export_Gids1_Bloodbank.xls\n"
							+ "args[1] /Users/Roan/Work/GIDS_8_may/Cohorts/Bloodbank/Export_Gids2_Bloodbank_dataset125_2013-05-08.csv/\n"
							+ "args[2] /Users/Roan/logger.txt");
			return;
		}
		Validator vc = new Validator();
		BufferedWriter logger = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[2])));
		try
		{
			logger.write("file1: " + args[0] + "\nfile2: " + args[1]);
			vc.check(args[0], args[1], logger);
			logger.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			IOUtils.closeQuietly(logger);
		}
	}
}
