package org.molgenis.api;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import java.util.HashSet;
import java.util.Set;
import org.molgenis.api.convert.QueryConverter;
import org.molgenis.api.convert.QueryRsqlVisitor;
import org.molgenis.api.convert.SortConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping(ApiNamespace.API_PATH + "/**").allowedMethods("*");
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(sortConverter());
    registry.addConverter(queryConverter());
  }

  private SortConverter sortConverter() {
    return new SortConverter();
  }

  private QueryConverter queryConverter() {
    return new QueryConverter(rsqlParser(), new QueryRsqlVisitor());
  }

  private RSQLParser rsqlParser() {
    Set<ComparisonOperator> operators = new HashSet<>(RSQLOperators.defaultOperators());
    operators.add(new ComparisonOperator("=q=", false));
    operators.add(new ComparisonOperator("=sq=", false));
    operators.add(new ComparisonOperator("=like=", false));
    operators.add(new ComparisonOperator("=notlike=", false));
    return new RSQLParser(operators);
  }
}
