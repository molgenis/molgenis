package org.molgenis.api.data.v3;

import static java.util.Objects.requireNonNull;

import org.molgenis.api.convert.SelectionConverter;
import org.molgenis.util.i18n.PropertiesMessageSource;
import org.molgenis.web.rsql.RSQLValueParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Import(RSQLValueParser.class)
@Configuration
public class DataApiV3Config implements WebMvcConfigurer {
  private final RSQLValueParser rsqlValueParser;

  public DataApiV3Config(RSQLValueParser rsqlValueParser) {
    this.rsqlValueParser = requireNonNull(rsqlValueParser);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(selectionConverter());
  }

  @Bean
  public SelectionConverter selectionConverter() {
    return new SelectionConverter();
  }

  @Bean
  public QueryV3Mapper queryV3Mapper() {
    return new QueryV3Mapper(rsqlValueParser);
  }

  @Bean
  public FetchMapper fetchMapper() {
    return new FetchMapper();
  }

  @Bean
  public SortV3Mapper sortV3Mapper() {
    return new SortV3Mapper();
  }
}
