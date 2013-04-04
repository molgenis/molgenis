package org.molgenis.gids;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.util.tuple.Tuple;

public class Validation_CompareFile

{
	List<String> listOfHeadersFileToCompare = new ArrayList<String>();
	HashMap<String, Tuple> hashFileToCompare = new HashMap<String, Tuple>();

	public void bla(ExcelSheetReader excelSheetReaderFileToCompare) throws IOException

	{
		Iterator<String> iterableFileToCompare = excelSheetReaderFileToCompare.colNamesIterator();

		while (iterableFileToCompare.hasNext())
		{
			String header = iterableFileToCompare.next().toLowerCase();
			listOfHeadersFileToCompare.add(header);
		}

		for (Tuple t : excelSheetReaderFileToCompare)
		{
			hashFileToCompare.put(t.getString("id_sample"), t);
		}
	}

	public List<String> getListOfHeadersFileToCompare()
	{
		return listOfHeadersFileToCompare;
	}

	public HashMap<String, Tuple> getHashFileToCompare()
	{
		return hashFileToCompare;
	}

}
