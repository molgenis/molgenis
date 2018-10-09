package org.molgenis.data.excel;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExcelL10nConfig {
  public static final String NAMESPACE = "excel";

  @Bean
  public PropertiesMessageSource excelMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
