package org.molgenis.data.file;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileConfig {
  @Bean
  public PropertiesMessageSource fileMessageSource() {
    return new PropertiesMessageSource("file");
  }
}
