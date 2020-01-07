package org.molgenis.api.data;

import static java.util.Objects.requireNonNull;

import org.molgenis.web.rsql.RSQLValueParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Import(RSQLValueParser.class)
@Configuration
public class DataApiConfig implements WebMvcConfigurer {
  private final RSQLValueParser rsqlValueParser;

  public DataApiConfig(RSQLValueParser rsqlValueParser) {
    this.rsqlValueParser = requireNonNull(rsqlValueParser);
  }

  @Bean
  public QueryMapper queryMapper() {
    return new QueryMapper(rsqlValueParser);
  }

  @Bean
  public SortMapper sortMapper() {
    return new SortMapper();
  }
}
