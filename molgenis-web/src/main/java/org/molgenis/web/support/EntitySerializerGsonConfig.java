package org.molgenis.web.support;

import org.molgenis.data.Entity;
import org.molgenis.gson.GsonBuilderCustomizer;
import org.molgenis.gson.GsonConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(GsonConfig.class)
public class EntitySerializerGsonConfig {
  @Bean
  public GsonBuilderCustomizer entitySerializerGsonBuilderCustomizer() {
    return builder -> builder.registerTypeHierarchyAdapter(Entity.class, new EntitySerializer());
  }
}
