package org.molgenis.tools.compare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * 
 * put in a hashmap
 */
public class ValidationFile

{
	private final List<String> listOfHeaders = new ArrayList<String>();
	private final LinkedHashMap<String, Entity> hash = new LinkedHashMap<String, Entity>();

	public void readFile(Repository<? extends Entity> excelSheetReader, String identifier, String identifier2)
			throws IOException
	{
		Iterable<String> iterableFileToCompare = Iterables.transform(excelSheetReader.getAttributes(),
				new Function<AttributeMetaData, String>()
				{
					@Override
					public String apply(AttributeMetaData input)
					{
						return input.getName();
					}

				});

		Iterator<String> iteratorFileToCompare = iterableFileToCompare.iterator();

		while (iteratorFileToCompare.hasNext())
		{
			String header = iteratorFileToCompare.next().toLowerCase();
			listOfHeaders.add(header);
		}

		for (Entity entity : excelSheetReader)
		{
			hash.put(entity.getString(identifier) + "_" + entity.getString(identifier2), entity);
		}
	}

	public List<String> getListOfHeaders()
	{
		return listOfHeaders;
	}

	public HashMap<String, Entity> getHash()
	{
		return hash;
	}

}
