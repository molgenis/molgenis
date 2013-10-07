package org.molgenis.data;

import org.molgenis.data.convert.DateToStringConverter;
import org.molgenis.data.convert.StringToDateConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

@Configuration
public class DataConfig
{
	@Bean
	public ConversionService converionService()
	{
		DefaultConversionService conversionService = new DefaultConversionService();
		conversionService.addConverter(new DateToStringConverter());
		conversionService.addConverter(new StringToDateConverter());

		return conversionService;
	}

}
