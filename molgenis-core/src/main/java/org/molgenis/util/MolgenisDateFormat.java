package org.molgenis.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static java.lang.String.format;

public class MolgenisDateFormat
{
	// TODO FIXME Do system default!
	private static final String DEFAULT_TIMEZONE_ID = "Europe/Amsterdam";

	private static final String DATEFORMAT_DATE = "yyyy-MM-dd";
	private static final String DATEFORMAT_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final String DATEFORMAT_DATETIME_SIMPLE = "yyyy-MM-dd HH:mm:ss";

	public static SimpleDateFormat getDateFormat()
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATEFORMAT_DATE);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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
