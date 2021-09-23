package org.molgenis.gson;

import com.baggonius.gson.immutable.ImmutableListDeserializer;
import com.baggonius.gson.immutable.ImmutableMapDeserializer;
import com.baggonius.gson.immutable.ImmutableSetDeserializer;
import com.baggonius.gson.optional.OptionalTypeFactory;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfig {
  @Bean
  public Gson gson(
      List<GsonBuilderCustomizer> customizers, List<TypeAdapterFactory> typeAdapterFactories) {
    var builder = new GsonBuilder();
    typeAdapterFactories.forEach(builder::registerTypeAdapterFactory);
    customizers.forEach(customizer -> customizer.customize(builder));
    return builder.create();
  }

  @Bean
  public TypeAdapterFactory optionalTypeAdapterFactory() {
    return OptionalTypeFactory.forJDK();
  }

  @Bean
  public TypeAdapterFactory autoValueTypeAdapterFactory() {
    return new AutoValueTypeAdapterFactory();
  }

  @Bean
  public GsonBuilderCustomizer javaTimeGsonCustomizer() {
    return Converters::registerAll;
  }

  @Bean
  public GsonBuilderCustomizer immutableCollectionGsonCustomizer() {
    return builder -> {
      builder.registerTypeHierarchyAdapter(ImmutableList.class, new ImmutableListDeserializer());
      builder.registerTypeHierarchyAdapter(ImmutableSet.class, new ImmutableSetDeserializer());
      builder.registerTypeHierarchyAdapter(ImmutableMap.class, new ImmutableMapDeserializer());
    };
  }

  @Bean
  public GsonBuilderCustomizer disableHtmlEscapingGsonCustomizer() {
    return GsonBuilder::disableHtmlEscaping;
  }
}
