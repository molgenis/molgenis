package org.molgenis.util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class MolgenisDateFormat
{
	private static final String DEFAULT_TIMEZONE_ID = "Europe/Amsterdam";

	public static final String DATEFORMAT_DATE = "yyyy-MM-dd";
	public static final String DATEFORMAT_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final String DATEFORMAT_DATETIME_SIMPLE = "yyyy-MM-dd HH:mm:ss";

	public static SimpleDateFormat getDateFormat()
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATEFORMAT_DATE);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE_ID));
		return simpleDateFormat;
	}

	public static SimpleDateFormat getDateTimeFormat()
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATEFORMAT_DATETIME);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE_ID));
		return simpleDateFormat;
	}

	public static SimpleDateFormat getDateTimeFormatSimple()
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATEFORMAT_DATETIME_SIMPLE);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE_ID));
		return simpleDateFormat;
	}
}
