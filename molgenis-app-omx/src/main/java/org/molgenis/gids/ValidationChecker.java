package org.molgenis.gids;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.molgenis.io.excel.ExcelReader;
import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.util.tuple.Tuple;

public class ValidationChecker
{

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{

		ExcelReader excelReaderFile1 = new ExcelReader(new File(args[0]));

		ExcelSheetReader excelSheetReaderFile1 = excelReaderFile1.getSheet(1);

		ExcelReader excelReaderFile2 = new ExcelReader(new File(args[1]));

		ExcelSheetReader excelSheetReaderFile2 = excelReaderFile2.getSheet(0);

		Iterator<String> iterable = excelSheetReaderFile2.colNamesIterator();
		List<String> listOfHeaders = new ArrayList<String>();
		while (iterable.hasNext())
		{
			String header = iterable.next();
			listOfHeaders.add(header);

		}
		Iterator<String> iterableJ = excelSheetReaderFile1.colNamesIterator();
		List<String> listOfHeadersJules = new ArrayList<String>();
		while (iterableJ.hasNext())
		{

			String header = iterable.next();
			listOfHeaders.add(header);

		}

		System.out.println("Sample ID\tFeature\tJules\tGIDS");
		try
		{

			HashMap<String, Tuple> hashJules = new HashMap<String, Tuple>();
			for (Tuple t : excelSheetReaderFile1)
			{
				hashJules.put(t.getString("id_sample"), t);
			}

			for (Tuple tuple : excelSheetReaderFile2)
			{

				if (hashJules.get(tuple.getString("id_sample")) != null)
				{
					compareRows(hashJules.get(tuple.getString("id_sample")), tuple, listOfHeaders);
				}
			}

		}
		finally
		{
			IOUtils.closeQuietly(excelReaderFile2);
			IOUtils.closeQuietly(excelReaderFile1);
		}
	}

	public static void compareRows(Tuple jules, Tuple gids, List<String> listOfHeaders)
	{
		for (String e : listOfHeaders)
		{
			if (jules.getString(e) != null)
			{
				if (!jules.getString(e).equals(gids.getString(e)))
				{
					System.out.println(jules.getString("id_sample") + "\t" + e + "\t" + jules.getString(e) + "\t"
							+ (gids.getString(e) == null ? "\tADDED" : gids.getString(e)));
				}

			}
		}
	}
}
