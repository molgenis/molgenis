package org.molgenis.api.data.v2;

import static java.util.Objects.requireNonNull;

import cz.jirutka.rsql.parser.RSQLParser;
import org.molgenis.web.rsql.AggregateQueryRsqlConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Import(RSQLParser.class)
@Configuration
public class RestConfig implements WebMvcConfigurer {
  private final RSQLParser rsqlParser;

  RestConfig(RSQLParser rsqlParser) {
    this.rsqlParser = requireNonNull(rsqlParser);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(attributeFilterConverter());
    registry.addConverter(aggregateQueryRsqlConverter());
  }

  @Bean
  public AttributeFilterConverter attributeFilterConverter() {
    return new AttributeFilterConverter();
  }

  @Bean
  public AggregateQueryRsqlConverter aggregateQueryRsqlConverter() {
    return new AggregateQueryRsqlConverter(rsqlParser);
  }
}
