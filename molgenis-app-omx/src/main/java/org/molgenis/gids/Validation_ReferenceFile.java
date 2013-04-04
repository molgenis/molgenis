package org.molgenis.gids;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.molgenis.io.excel.ExcelSheetReader;
import org.molgenis.util.tuple.Tuple;

public class Validation_ReferenceFile
{

	List<String> listOfHeadersReferenceFile = new ArrayList<String>();
	HashMap<String, Tuple> hashReference = new HashMap<String, Tuple>();

	public void bla(ExcelSheetReader excelSheetReaderReferenceFile) throws IOException

	{
		Iterator<String> iterableReferenceFile = excelSheetReaderReferenceFile.colNamesIterator();

		while (iterableReferenceFile.hasNext())
		{
			String header = iterableReferenceFile.next().toLowerCase();
			listOfHeadersReferenceFile.add(header);
		}

		for (Tuple t : excelSheetReaderReferenceFile)
		{
			hashReference.put(t.getString("id_sample"), t);
		}

	}

	public List<String> getListOfHeadersReferenceFile()
	{
		return listOfHeadersReferenceFile;
	}

	public HashMap<String, Tuple> getHashReference()
	{
		return hashReference;
	}

}
