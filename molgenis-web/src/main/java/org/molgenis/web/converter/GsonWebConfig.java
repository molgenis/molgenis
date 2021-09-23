package org.molgenis.web.converter;

import com.google.gson.Gson;
import org.molgenis.gson.GsonBuilderCustomizer;
import org.molgenis.gson.GsonConfig;
import org.molgenis.web.menu.model.MenuGsonConfig;
import org.molgenis.web.support.EntitySerializerGsonConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

@Configuration
@Import({GsonConfig.class, MenuGsonConfig.class, EntitySerializerGsonConfig.class})
public class GsonWebConfig {

  @Bean
  public GsonHttpMessageConverter gsonHttpMessageConverter(Gson gson) {
    return new GsonHttpMessageConverter(gson);
  }

  @Bean
  public GsonBuilderCustomizer prettyPrintGsonBuilderCustomizer(
      @Value("${environment:production}") String environment) {
    return builder -> {
      if (environment != null
          && (environment.equals("development") || environment.equals("test"))) {
        builder.setPrettyPrinting();
      }
    };
  }
}
