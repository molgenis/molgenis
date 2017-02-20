package org.molgenis.file.ingest.meta;

import java.util.HashMap;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;

public enum FileIngestType
{
	DOWNLOAD, BUCKET;

	private static final HashMap<String, FileIngestType> strValMap;

	public static FileIngestType toEnum(String valueString)
	{
		return strValMap.get(valueString);
	}

	static
	{
		FileIngestType[] types = FileIngestType.values();
		strValMap = newHashMapWithExpectedSize(types.length);
		for (FileIngestType type : types)
		{
			strValMap.put(type.toString(), type);
		}
	}

}
