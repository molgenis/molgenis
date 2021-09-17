package org.molgenis.data.row.permissions;

import org.molgenis.util.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataRowPermissionsI18nConfig {
  public static final String NAMESPACE = "data-row-permissions";

  @Bean
  public PropertiesMessageSource dataRowPermissionsI18nMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
