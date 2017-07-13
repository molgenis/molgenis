package org.molgenis.charts;

import java.util.HashMap;
import java.util.Map;

/**
 * MimeTypes Map
 */
public class MimeTypes
{
	private static final Map<String, String> mimeTypesMap = new HashMap<>();

	static
	{
		mimeTypesMap.put("svg", "image/svg+xml");
		mimeTypesMap.put("csv", "text/csv");
	}

	public static String getContentType(String extension)
	{

		String contentType = mimeTypesMap.get(extension);
		if (contentType == null)
		{
			throw new MolgenisChartException("Unknown file extension [" + extension + "]");
		}

		return contentType;
	}
}
