package org.molgenis.data;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.convert.StringToDateTimeConverter;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.ListEscapeUtils;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.support.DefaultConversionService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;

@SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "We want to return Boolean.TRUE, Boolean.FALSE or null")
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

	/**
	 * Convert value to the type based on the given attribute.
	 *
	 * @param source value to convert
	 * @param attr   attribute that defines the type of the converted value
	 * @return converted value or the input value if the attribute type is a reference type
	 */
	public static Object convert(Object source, Attribute attr)
	{
		try
		{
			switch (attr.getDataType())
			{
				case BOOL:
					return toBoolean(source);
				case XREF:
				case CATEGORICAL:
				case CATEGORICAL_MREF:
				case MREF:
				case FILE:
				case ONE_TO_MANY:
					return source;
				case COMPOUND:
					throw new UnsupportedOperationException();
				case DATE:
					return toLocalDate(source);
				case DATE_TIME:
					return toInstant(source);
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
		catch (ConversionFailedException cfe)
		{
			throw new MolgenisDataException(
					String.format("Conversion failure in entity type [%s] attribute [%s]; %s", attr.getEntity().getId(),
							attr.getName(), cfe.getMessage()));
		}
	}

	public static String toString(Object source)
	{
		if (source == null) return null;
		if (source instanceof String) return (String) source;
		if (source instanceof Entity)
		{
			Object labelValue = ((Entity) source).getLabelValue();
			return labelValue != null ? labelValue.toString() : null;
		}
		if (source instanceof List)
		{
			StringBuilder sb = new StringBuilder();
			for (Object obj : (List<?>) source)
			{
				if (sb.length() > 0) sb.append(",");
				sb.append(toString(obj));
			}

			return sb.toString();
		}

		if (getConversionService() == null) return source.toString();

		try
		{
			return convert(source, String.class);
		}
		catch (ConverterNotFoundException e)
		{
			return source.toString();
		}
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

	public static LocalDate toLocalDate(Object source)
	{
		if (source == null) return null;
		if (source instanceof LocalDate) return (LocalDate) source;
		return convert(source, LocalDate.class);
	}

	public static Instant toInstant(Object source)
	{
		if (source == null) return null;
		if (source instanceof Instant) return (Instant) source;
		return convert(source, Instant.class);
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
		else if (source instanceof Iterable<?>)
		{
			return stream(((Iterable<?>) source).spliterator(), false).map(obj ->
			{
				Object objValue;
				if (obj instanceof Entity)
				{
					objValue = ((Entity) obj).getIdValue();
				}
				else
				{
					objValue = obj;
				}
				return DataConverter.toString(objValue);
			}).collect(Collectors.toList());
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
			List<Object> result = new ArrayList<>();
			result.addAll(Arrays.asList(((String) source).split(",")));
			return result;
		}
		else return Arrays.asList(source);
	}

	public static List<Integer> toIntList(Object source)
	{
		if (source == null) return null;
		else if (source instanceof String)
		{
			List<String> stringList = ListEscapeUtils.toList((String) source);
			List<Integer> intList = new ArrayList<>();
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
			ArrayList<Integer> intList = new ArrayList<>();
			for (Object o : (Iterable<?>) source)
			{
				if (o instanceof Entity)
				{
					o = ((Entity) o).getIdValue();
				}
				intList.add(toInt(o));
			}
			return intList;
		}
		else if (source instanceof Integer) return new ArrayList<>(Arrays.asList((Integer) source));
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
				((DefaultConversionService) conversionService).addConverter(new StringToDateConverter());
				((DefaultConversionService) conversionService).addConverter(new StringToDateTimeConverter());
			}
			else
			{
				conversionService = ApplicationContextProvider.getApplicationContext().getBean(ConversionService.class);
			}

		}

		return conversionService;
	}
}
