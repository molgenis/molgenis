package org.molgenis.util;

import java.text.SimpleDateFormat;

public class MolgenisDateFormat
{
	public static final String DATEFORMAT_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZ";

	public static SimpleDateFormat getDateFormat()
	{
		return new SimpleDateFormat(DATEFORMAT_DATETIME);
	}

}
