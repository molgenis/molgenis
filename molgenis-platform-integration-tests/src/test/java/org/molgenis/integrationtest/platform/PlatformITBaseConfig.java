package org.molgenis.integrationtest.platform;

import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.convert.StringToDateTimeConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

@Configuration
public class PlatformITBaseConfig {
  @Bean
  public ConversionService conversionService() {
    DefaultConversionService defaultConversionService = new DefaultConversionService();
    defaultConversionService.addConverter(new StringToDateConverter());
    defaultConversionService.addConverter(new StringToDateTimeConverter());
    return defaultConversionService;
  }
}
