package org.molgenis.gids.tools.compare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.util.tuple.Tuple;

/**
 * This class is used to read the 2nd file (the compare file). The data will be
 * put in a hashmap
 */
public class ValidationFile

{
	private final List<String> listOfHeaders = new ArrayList<String>();
	private final LinkedHashMap<String, Tuple> hash = new LinkedHashMap<String, Tuple>();

	public void bla(ExcelSheetReader excelSheetReader, String identifier) throws IOException

	{
		Iterator<String> iterableFileToCompare = excelSheetReader.colNamesIterator();

		while (iterableFileToCompare.hasNext())
		{
			String header = iterableFileToCompare.next().toLowerCase();
			listOfHeaders.add(header);
		}

		for (Tuple t : excelSheetReader)
		{
			hash.put(t.getString(identifier), t);
		}
	}

	public List<String> getListOfHeaders()
	{
		return listOfHeaders;
	}

	public HashMap<String, Tuple> getHash()
	{
		return hash;
	}

}
