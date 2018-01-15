package org.molgenis.data.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

public class MolgenisDateFormat
{

	public static final String FAILED_TO_PARSE_ATTRIBUTE_AS_DATE_MESSAGE = "Failed to parse attribute [%s] value [%s] as date. Valid date format is [YYYY-MM-DD].";
	public static final String FAILED_TO_PARSE_ATTRIBUTE_AS_DATETIME_MESSAGE = "Failed to parse attribute [%s] value [%s] as datetime. Valid datetime format is [YYYY-MM-DDThh:mm:ssZ]";
	private static final String LOOSE_PARSER_FORMAT = "[yyyy-MM-dd]['T'[HHmmss][HHmm][HH:mm:ss][HH:mm][.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]][OOOO][O][z][XXXXX][XXXX]['['VV']']";

	/**
	 * Tries to parse a value representing a LocalDate.
	 * Tries many formats, but does require that the date month and year are provided in yyyy-mm-dd form.
	 * If too much information is provided, such as time and/or time zone, will simply truncate those away.
	 *
	 * @param value the value to parse.
	 * @return the parsed {@link LocalDate}
	 * @throws DateTimeParseException if parsing fails
	 */
	public static LocalDate parseLocalDate(String value)
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
	public static Instant parseInstant(String value)
	{
		TemporalAccessor temporalAccessor = DateTimeFormatter.ofPattern(LOOSE_PARSER_FORMAT)
															 .parseBest(value, ZonedDateTime::from, LocalDateTime::from,
																	 LocalDate::from);
		if (temporalAccessor instanceof ZonedDateTime)
		{
			return ((ZonedDateTime) temporalAccessor).toInstant();
		}
		if (temporalAccessor instanceof LocalDateTime)
		{
			return ((LocalDateTime) temporalAccessor).atZone(ZoneId.systemDefault()).toInstant();
		}
		return ((LocalDate) temporalAccessor).atStartOfDay(ZoneId.systemDefault()).toInstant();
	}

}
