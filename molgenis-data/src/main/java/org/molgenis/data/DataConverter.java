package org.molgenis.data;

import org.molgenis.util.ApplicationContextProvider;
import org.springframework.core.convert.ConversionService;

public class DataConverter
{
	private static ConversionService conversionService;

	@SuppressWarnings("unchecked")
	public static <T> T convert(Object source, Class<T> targetType)
	{
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
}
