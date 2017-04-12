package org.molgenis.util;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.TimeZone;

public class MolgenisDateFormat
{
	// TODO FIXME Switch to system default
	private static final String DEFAULT_TIMEZONE_ID = "Europe/Amsterdam";

	private static final String DATEFORMAT_DATE = "yyyy-MM-dd";
	private static final String DATEFORMAT_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final String DATEFORMAT_DATETIME_SIMPLE = "yyyy-MM-dd HH:mm:ss";

	private static final String LOOSE_PARSER_FORMAT = "[yyyy-MM-dd]['T'[HHmmss][HHmm][HH:mm:ss][HH:mm][.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]][OOOO][O][z][XXXXX][XXXX]['['VV']']";

	/**
	 * Retrieves the formatter used to format AttributeType.DATE attributes.
	 * The values it formats are {@link java.time.LocalDate}s, and they will be formatted without a time zone.
	 */
	public static DateTimeFormatter getLocalDateFormatter()
	{
		return DateTimeFormatter.ISO_LOCAL_DATE;
	}

	/**
	 * Retrieves the formatter used to format AttributeType.DATE_TIME attributes.
	 * The values are {@link java.time.Instant}s, and they will be formatted as UTC.
	 */
	public static DateTimeFormatter getDateTimeFormatter()
	{
		return DateTimeFormatter.ISO_INSTANT;
	}

	/**
	 * Tries to parse a value representing a LocalDate.
	 * Tries many formats, but does require that the date month and year are provided in yyyy-mm-dd form.
	 * If too much information is provided, such as time and/or time zone, will simply truncate those away.
	 *
	 * @param value the value to parse.
	 * @return the parsed {@link LocalDate}
	 * @throws DateTimeParseException if parsing fails
	 */
	public static LocalDate parseLocalDate(String value) throws DateTimeParseException
	{
		TemporalAccessor temporalAccessor = DateTimeFormatter.ofPattern(LOOSE_PARSER_FORMAT)
				.parseBest(value, ZonedDateTime::from, LocalDate::from);

		if (temporalAccessor instanceof ZonedDateTime)
		{
			return ((ZonedDateTime) temporalAccessor).toLocalDate();
		}
		return (LocalDate) temporalAccessor;
	}

	/**
	 * Tries to parse a value representing an {@link Instant}.
	 * Tries many formats, but does require that the date month and year are provided in yyyy-mm-dd form.
	 * If too little information is provided, will fill in the start of day and getDefaultZoneId() as default values.
	 *
	 * @param value the value to parse.
	 * @return the parsed {@link Instant}
	 * @throws DateTimeParseException if parsing fails
	 */
	public static Instant parseInstant(String value) throws DateTimeParseException
	{
		TemporalAccessor temporalAccessor = DateTimeFormatter.ofPattern(LOOSE_PARSER_FORMAT)
				.parseBest(value, ZonedDateTime::from, LocalDateTime::from, LocalDate::from);
		if (temporalAccessor instanceof ZonedDateTime)
		{
			return ((ZonedDateTime) temporalAccessor).toInstant();
		}
		if (temporalAccessor instanceof LocalDateTime)
		{
			return ((LocalDateTime) temporalAccessor).atZone(getDefaultZoneId()).toInstant();
		}
		return ((LocalDate) temporalAccessor).atStartOfDay(getDefaultZoneId()).toInstant();
	}

	/**
	 * @return the {@link ZoneId} to use when communicating with the user.
	 */
	public static ZoneId getDefaultZoneId()
	{
		return ZoneId.of(DEFAULT_TIMEZONE_ID);
	}

	@Deprecated
	public static SimpleDateFormat getDateFormat()
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATEFORMAT_DATE);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return simpleDateFormat;
	}

	@Deprecated
	public static SimpleDateFormat getDateTimeFormat()
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATEFORMAT_DATETIME);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE_ID));
		return simpleDateFormat;
	}

	@Deprecated
	public static String getDateTimeFormatPattern()
	{
		return DATEFORMAT_DATETIME;
	}

	@Deprecated
	public static SimpleDateFormat getDateTimeFormatSimple()
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATEFORMAT_DATETIME_SIMPLE);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE_ID));
		return simpleDateFormat;
	}
}
