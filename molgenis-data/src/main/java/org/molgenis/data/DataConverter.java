package org.molgenis.data;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.ListEscapeUtils;
import org.springframework.core.convert.ConversionService;

public class DataConverter
{
	private static ConversionService conversionService;

	@SuppressWarnings("unchecked")
	public static <T> T convert(Object source, Class<T> targetType)
	{
		if (source == null)
		{
			return null;
		}

		if (targetType.isAssignableFrom(source.getClass()))
		{
			return (T) source;
		}

		if (conversionService == null)
		{
			conversionService = ApplicationContextProvider.getApplicationContext().getBean(ConversionService.class);
		}

		return conversionService.convert(source, targetType);
	}

	public static String toString(Object source)
	{
		if (source == null) return null;
		if (source instanceof String) return (String) source;
		return convert(source, String.class);
	}

	public static Integer toInt(Object source)
	{
		if (source == null) return null;
		if (source instanceof Integer) return (Integer) source;
		return convert(source, Integer.class);
	}

	public static Long toLong(Object source)
	{
		if (source == null) return null;
		if (source instanceof Long) return (Long) source;
		return convert(source, Long.class);
	}

	public static Boolean toBoolean(Object source)
	{
		if (source == null) return null;
		if (source instanceof Boolean) return (Boolean) source;
		return convert(source, Boolean.class);
	}

	public static Double toDouble(Object source)
	{
		if (source == null) return null;
		if (source instanceof Double) return (Double) source;
		return convert(source, Double.class);
	}

	public static java.sql.Date toDate(Object source)
	{
		if (source == null) return null;
		if (source instanceof java.sql.Date) return (java.sql.Date) source;
		return convert(source, java.sql.Date.class);
	}
	
	public static java.util.Date toUtilDate(Object source)
	{
		if (source == null) return null;
		if (source instanceof java.util.Date) return (java.util.Date) source;
		return convert(source, java.util.Date.class);
	}

	public static Timestamp toTimestamp(Object source)
	{
		if (source == null) return null;
		else if (source instanceof Timestamp) return (Timestamp) source;
		else if (source instanceof Date) return new Timestamp(((Date) source).getTime());
		return new Timestamp(convert(source, Date.class).getTime());
	}

	@SuppressWarnings("unchecked")
	public static List<String> toList(Object source)
	{
		if (source == null) return null;
		else if (source instanceof List<?>) return (List<String>) source;
		else if (source instanceof String) return ListEscapeUtils.toList((String) source);
		else return ListEscapeUtils.toList(source.toString());
	}

	// TODO: Improve code of getIntList (maybe merge with getList above?)
	public static List<Integer> toIntList(Object source)
	{
		if (source == null) return null;
		else if (source instanceof List<?>)
		{
			// it seems we need explicit cast to Int sometimes
			ArrayList<Integer> intList = new ArrayList<Integer>();
			for (Object o : (List<?>) source)
			{
				if (o instanceof Integer) intList.add((Integer) o);
				else if (o instanceof String) intList.add(Integer.parseInt((String) o));
			}
			return intList;
		}
		else if (source instanceof Integer) return new ArrayList<Integer>(Arrays.asList((Integer) source));
		else return null;
	}

}
