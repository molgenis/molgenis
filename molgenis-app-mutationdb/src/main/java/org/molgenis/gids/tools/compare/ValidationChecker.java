package org.molgenis.gids.tools.compare;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.util.tuple.Tuple;

/**
 * This script is created to compare 2 excelfiles. The files are given via the
 * arguments
 */

public class ValidationChecker
{

	/**
	 * @param args
	 * @throws IOException
	 */
	public BufferedWriter logger = null;
	private final static String IDENTIFIER = "id_sample";

	public void check(String file1, String file2, BufferedWriter logger) throws IOException
	{
		this.logger = logger;
		LinkedHashMap<String, String> hashCheckedValues = new LinkedHashMap<String, String>();

		// Make Object Reference
		ValidationFile ref = new ValidationFile();
		ExcelReader excelReaderReferenceFile = new ExcelReader(new File(file1));
		ExcelSheetReader excelSheetReaderReferenceFile = excelReaderReferenceFile.getSheet(0);
		ref.bla(excelSheetReaderReferenceFile, IDENTIFIER);

		// Make Object FileToCompare
		ValidationFile com = new ValidationFile();
		ExcelReader excelReaderFileToCompare = new ExcelReader(new File(file2));
		ExcelSheetReader excelSheetReaderFileToCompare = excelReaderFileToCompare.getSheet(0);
		com.bla(excelSheetReaderFileToCompare, IDENTIFIER);
		boolean noUniqueColums = false;
		// Make list for shared headers

		List<String> listOfSharedHeaders = new ArrayList<String>();
		// Compare the headers of Reference file with fileToCompare
		// Add Reference headers to the listOfSharedHeaders
		System.out.println("### Unique columns");
		for (String o : ref.getListOfHeaders())
		{
			listOfSharedHeaders.add(o);
			if (!com.getListOfHeaders().contains(o))
			{
				System.out.println("In file1: " + o);
				noUniqueColums = true;
			}
		}

		// Compare the headers of fileToCompare file with Reference
		// Add Reference headers to the listOfSharedHeaders
		for (String o : com.getListOfHeaders())
		{
			if (!listOfSharedHeaders.contains(o))
			{
				listOfSharedHeaders.add(o);
			}
			if (!ref.getListOfHeaders().contains(o))
			{
				System.out.println("In file2: " + o);
				noUniqueColums = true;
			}
		}

		if (noUniqueColums == false)
		{
			System.out.println("###There are no added/deleted columns\n");
		}

		// print the header
		System.out.println("\n###Comparing the values ");
		System.out.println("Sample ID\tFeature\tFile1\tFile2");

		try
		{
			for (Entry<String, Tuple> entry : ref.getHash().entrySet())
			{
				if (com.getHash().get(entry.getValue().getString(IDENTIFIER)) != null)
				{
					compareRows(com.getHash().get(entry.getValue().getString(IDENTIFIER)), entry.getValue(),
							listOfSharedHeaders, hashCheckedValues, logger);
				}

			}

			for (Entry<String, Tuple> entry : com.getHash().entrySet())
			{
				if (ref.getHash().get(entry.getValue().getString(IDENTIFIER)) != null)
				{
					compareRows(entry.getValue(), ref.getHash().get(entry.getValue().getString(IDENTIFIER)),
							listOfSharedHeaders, hashCheckedValues, logger);
				}

			}

			System.out.println("\n###Unique samples in file1");
			for (Entry<String, Tuple> entry : ref.getHash().entrySet())
			{
				if (!com.getHash().containsKey(entry.getKey()))
				{
					System.out.println(entry.getKey());
					// listOfUniqueSampleIdsReference.add(entry.getKey());
				}
			}
			System.out.println("\n###Unique samples in file2 ");
			for (Entry<String, Tuple> entry : com.getHash().entrySet())
			{
				if (!ref.getHash().containsKey(entry.getKey()))
				{
					System.out.println(entry.getKey());
					// listOfUniqueSampleIdsFilesToCompare.add(entry.getKey());
				}
			}

		}
		finally
		{
			IOUtils.closeQuietly(excelReaderFileToCompare);
			IOUtils.closeQuietly(excelReaderReferenceFile);
		}
	}

	public static void compareRows(Tuple firstTuple, Tuple secondTuple, List<String> listOfHeaders,
			LinkedHashMap<String, String> hashCheckedValues, BufferedWriter logger) throws IOException
	{

		for (String e : listOfHeaders)
		{
			if (firstTuple.getString(e) == null && secondTuple.getString(e) == null)
			{

			}
			else if (firstTuple.getString(e) != null && secondTuple.getString(e) == null)
			{
				if (!hashCheckedValues.containsKey(firstTuple.getString(IDENTIFIER) + e)
						&& !hashCheckedValues.containsValue(secondTuple))
				{
					System.out.println(firstTuple.getString(IDENTIFIER) + "\t" + e + "\t"
							+ (secondTuple.getString(e) == null ? ("\tAdded") : secondTuple.getString(e)) + "\t"
							+ firstTuple.getString(e));
					hashCheckedValues.put(firstTuple.getString(IDENTIFIER) + e, secondTuple.getString(e));
				}

			}
			else if (firstTuple.getString(e) == null && secondTuple.getString(e) != null)
			{
				if (!hashCheckedValues.containsKey(firstTuple.getString(IDENTIFIER) + e)
						&& !hashCheckedValues.containsValue(secondTuple))
				{
					System.out.println(firstTuple.getString(IDENTIFIER)
							+ "\t"
							+ e
							+ "\t"
							+ (secondTuple.getString(e) == null ? ("\tAdded in the " + "file2") : secondTuple
									.getString(e)) + "\t" + firstTuple.getString(e));
					hashCheckedValues.put(firstTuple.getString(IDENTIFIER) + e, secondTuple.getString(e));
				}
			}
			else
			{
				if (!firstTuple.getString(e).equals(secondTuple.getString(e)))
				{
					if (!hashCheckedValues.containsKey(firstTuple.getString(IDENTIFIER) + e)
							&& !hashCheckedValues.containsValue(secondTuple))
					{

						hashCheckedValues.put(firstTuple.getString(IDENTIFIER) + e, secondTuple.getString(e));
						System.out.println(firstTuple.getString(IDENTIFIER)
								+ "\t"
								+ e
								+ "\t"
								+ (secondTuple.getString(e) == null ? ("\tAdded in the " + "Reference") : secondTuple
										.getString(e)) + "\t" + firstTuple.getString(e)

						);
					}

				}

			}

		}

	}
}
