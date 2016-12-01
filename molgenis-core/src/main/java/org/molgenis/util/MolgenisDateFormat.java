package org.molgenis.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static java.lang.String.format;

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

	/**
	 * Format a date to a MOLGENIS date
	 *
	 * @param date
	 * @return Date
	 */
	public static Date formatDate(Date date)
	{
		if (null == date) return date;
		SimpleDateFormat format = MolgenisDateFormat.getDateFormat();
		try
		{
			return format.parse(format.format(date));
		}
		catch (ParseException pe)
		{
			throw new RuntimeException(format("Value [%s] does not match date format [%s]", format.format(date),
					MolgenisDateFormat.DATEFORMAT_DATE));
		}
	}

	/**
	 * Format a date to a MOLGENIS dateTime
	 *
	 * @param date
	 * @return Date
	 */
	public static Date formatDateTime(Date date)
	{
		if (null == date) return date;
		SimpleDateFormat format = MolgenisDateFormat.getDateTimeFormat();
		try
		{
			return format.parse(format.format(date));
		}
		catch (ParseException pe)
		{
			throw new RuntimeException(format("Value [%s] does not match date time format [%s]", format.format(date),
					MolgenisDateFormat.DATEFORMAT_DATETIME));
		}
	}
}
