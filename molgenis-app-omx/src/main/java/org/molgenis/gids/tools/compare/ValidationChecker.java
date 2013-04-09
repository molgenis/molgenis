package org.molgenis.gids.tools.compare;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
	public static void main(String[] args) throws IOException
	{
		HashMap<String, String> hashCheckedValues = new HashMap<String, String>();

		// Make Object Reference
		Validation_ReferenceFile ref = new Validation_ReferenceFile();
		ExcelReader excelReaderReferenceFile = new ExcelReader(new File(args[0]));
		ExcelSheetReader excelSheetReaderReferenceFile = excelReaderReferenceFile.getSheet(0);
		ref.bla(excelSheetReaderReferenceFile);

		// Make Object FileToCompare
		Validation_CompareFile com = new Validation_CompareFile();
		ExcelReader excelReaderFileToCompare = new ExcelReader(new File(args[1]));
		ExcelSheetReader excelSheetReaderFileToCompare = excelReaderFileToCompare.getSheet(0);
		com.bla(excelSheetReaderFileToCompare);
		boolean noUniqueColums = false;
		// Make list for shared headers

		List<String> listOfSharedHeaders = new ArrayList<String>();
		// Compare the headers of Reference file with fileToCompare
		// Add Reference headers to the listOfSharedHeaders
		for (String o : ref.listOfHeadersReferenceFile)
		{
			listOfSharedHeaders.add(o);
			if (!com.getListOfHeadersFileToCompare().contains(o))
			{
				System.out.println("Unique columns in reference file: " + o);
				noUniqueColums = true;
			}
		}

		// Compare the headers of fileToCompare file with Reference
		// Add Reference headers to the listOfSharedHeaders
		for (String o : com.listOfHeadersFileToCompare)
		{
			if (!listOfSharedHeaders.contains(o))
			{
				listOfSharedHeaders.add(o);
			}
			if (!ref.getListOfHeadersReferenceFile().contains(o))
			{
				System.out.println("Unique columns in filetocompare file: " + o);
				noUniqueColums = true;
			}
		}

		if (noUniqueColums == false)
		{
			System.out.println("###There are no added/deleted columns\n");
		}

		// print the header
		System.out.println("###Comparing the values ");
		System.out.println("Sample ID\tFeature\tReference\tFileToCompare");

		try
		{
			for (Entry<String, Tuple> entry : ref.hashReference.entrySet())
			{
				if (com.hashFileToCompare.get(entry.getValue().getString("id_sample")) != null)
				{
					compareRows(com.hashFileToCompare.get(entry.getValue().getString("id_sample")), entry.getValue(),
							listOfSharedHeaders, hashCheckedValues);
				}

			}

			for (Entry<String, Tuple> entry : com.hashFileToCompare.entrySet())
			{
				if (ref.hashReference.get(entry.getValue().getString("id_sample")) != null)
				{
					compareRows(entry.getValue(), ref.hashReference.get(entry.getValue().getString("id_sample")),
							listOfSharedHeaders, hashCheckedValues);
				}

			}

			System.out.println("\n###Unique samples in Reference file");
			for (Entry<String, Tuple> entry : ref.hashReference.entrySet())
			{
				if (!com.hashFileToCompare.containsKey(entry.getKey()))
				{
					System.out.println(entry.getKey());
					// listOfUniqueSampleIdsReference.add(entry.getKey());
				}
			}
			System.out.println("\n###Unique samples in fileToCompare ");
			for (Entry<String, Tuple> entry : com.hashFileToCompare.entrySet())
			{
				if (!ref.hashReference.containsKey(entry.getKey()))
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
			HashMap<String, String> hashCheckedValues)
	{
		for (String e : listOfHeaders)
		{
			if (firstTuple.getString(e) != null)
			{
				if (secondTuple.getString(e) != null)
				{
					if (!firstTuple.getString(e).equals(secondTuple.getString(e)))
					{
						if (!hashCheckedValues.containsKey(e) && !hashCheckedValues.containsValue(secondTuple))
						{
							hashCheckedValues.put(e, secondTuple.getString(e));
							System.out
									.println(firstTuple.getString("id_sample")
											+ "\t"
											+ e
											+ "\t"
											+ firstTuple.getString(e)
											+ "\t"
											+ (secondTuple.getString(e) == null ? ("\tAdded in the " + "Reference") : secondTuple
													.getString(e)));
						}
					}
				}
			}
		}

	}
}
