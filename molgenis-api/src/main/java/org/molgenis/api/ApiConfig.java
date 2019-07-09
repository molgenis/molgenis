package org.molgenis.api;

import static java.util.Objects.requireNonNull;

import cz.jirutka.rsql.parser.RSQLParser;
import org.molgenis.api.convert.QueryConverter;
import org.molgenis.api.convert.SortConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiConfig implements WebMvcConfigurer {
  private final RSQLParser rsqlParser;

  public ApiConfig(RSQLParser rsqlParser) {
    this.rsqlParser = requireNonNull(rsqlParser);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(sortConverter());
    registry.addConverter(query());
  }

  @Bean
  public SortConverter sortConverter() {
    return new SortConverter();
  }

  @Bean
  public QueryConverter query() {
    return new QueryConverter(rsqlParser);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping(ApiNamespace.API_PATH + "/**").allowedMethods("*");
  }
}
