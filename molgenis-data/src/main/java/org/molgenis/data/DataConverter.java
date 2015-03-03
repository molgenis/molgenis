package org.molgenis.data;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.convert.DateToStringConverter;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.ListEscapeUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "We want to return Boolean.TRUE, Boolean.FALSE or null")
public class DataConverter
{
	private static ConversionService conversionService;

	public static boolean canConvert(Object source, Class<?> targetType)
	{
		try
		{
			convert(source, targetType);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}

	}

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

		return getConversionService().convert(source, targetType);
	}

	public static Object convert(Object source, AttributeMetaData attr)
	{
		switch (attr.getDataType().getEnumType())
		{
			case BOOL:
				return toBoolean(source);
			case XREF:
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case MREF:
				return source;
			case COMPOUND:
				throw new UnsupportedOperationException();
			case DATE:
				return toDate(source);
			case DATE_TIME:
				return toUtilDate(source);
			case DECIMAL:
				return toDouble(source);
			case INT:
				return toInt(source);
			case LONG:
				return toLong(source);
			default:
				return toString(source);

		}
	}

	public static String toString(Object source)
	{
		if (source == null) return null;
		if (source instanceof String) return (String) source;
		if (conversionService == null) return source.toString();
		if (source instanceof FieldType) return source.toString();
		if (source instanceof Entity) return ((Entity) source).getLabelValue();

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
		if (source instanceof java.util.Date) return new java.sql.Date(((java.util.Date) source).getTime());
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
		return new Timestamp(convert(source, java.util.Date.class).getTime());
	}

	public static Entity toEntity(Object source)
	{
		if (source == null) return null;
		if (source instanceof Entity) return (Entity) source;
		return convert(source, Entity.class);
	}

	@SuppressWarnings("unchecked")
	public static Iterable<Entity> toEntities(Object source)
	{
		if (source == null) return null;
		if (source instanceof Iterable) return (Iterable<Entity>) source;
		return null;
	}

	@SuppressWarnings("unchecked")
	public static List<String> toList(Object source)
	{
		if (source == null) return null;
		else if (source instanceof List<?>) return (List<String>) source;
		else if (source instanceof Iterable<?>)
		{
			return Lists.newArrayList(Iterables.transform((Iterable<Object>) source, new Function<Object, String>()
			{

				@Override
				public String apply(Object input)
				{
					return DataConverter.toString(input);
				}

			}));
		}
		else if (source instanceof String) return ListEscapeUtils.toList((String) source);
		else return ListEscapeUtils.toList(source.toString());
	}

	@SuppressWarnings("unchecked")
	public static List<Object> toObjectList(Object source)
	{
		if (source == null) return null;
		else if (source instanceof List) return (List<Object>) source;
		else if (source instanceof String)
		{
			List<Object> result = new ArrayList<Object>();
			for (String str : ((String) source).split(","))
				result.add(str);
			return result;
		}
		else return Arrays.asList(new Object[]
		{ source });
	}

	public static List<Integer> toIntList(Object source)
	{
		if (source == null) return null;
		else if (source instanceof String)
		{
			List<String> stringList = ListEscapeUtils.toList((String) source);
			List<Integer> intList = new ArrayList<Integer>();
			for (String s : stringList)
			{
				if (!StringUtils.isNumeric(s))
				{
					throw new IllegalArgumentException(s + " is not an integer");
				}
				intList.add(Integer.parseInt(s));
			}

			return intList;
		}
		else if (source instanceof Iterable<?>)
		{
			ArrayList<Integer> intList = new ArrayList<Integer>();
			for (Object o : (Iterable<?>) source)
			{
				intList.add(toInt(o));
			}
			return intList;
		}
		else if (source instanceof Integer) return new ArrayList<Integer>(Arrays.asList((Integer) source));
		else return null;
	}

	private static ConversionService getConversionService()
	{
		if (conversionService == null)
		{
			if (ApplicationContextProvider.getApplicationContext() == null)
			{
				// We are not in a Spring managed environment
				conversionService = new DefaultConversionService();
				((DefaultConversionService) conversionService).addConverter(new DateToStringConverter());
				((DefaultConversionService) conversionService).addConverter(new StringToDateConverter());
			}
			else
			{
				conversionService = ApplicationContextProvider.getApplicationContext().getBean(ConversionService.class);
			}

		}

		return conversionService;
	}
}
