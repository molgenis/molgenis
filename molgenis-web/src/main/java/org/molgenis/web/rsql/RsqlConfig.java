package org.molgenis.web.rsql;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RsqlConfig implements WebMvcConfigurer {
  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(rsqlQueryConverter());
  }

  @Bean
  public QueryRsqlConverter rsqlQueryConverter() {
    return new QueryRsqlConverter(rsqlParser());
  }

  @Bean
  public RSQLParser rsqlParser() {
    Set<ComparisonOperator> operators = RSQLOperators.defaultOperators();
    operators.add(new ComparisonOperator("=q=", false));
    operators.add(new ComparisonOperator("=notlike=", false));
    operators.add(new ComparisonOperator("=rng=", true));
    operators.add(new ComparisonOperator("=like=", false));
    return new RSQLParser(operators);
  }
}
