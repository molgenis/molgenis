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

  /**
   * Creates the {@link Gson} bean.
   *
   * <p>The trick is that high level modules can create beans of type GsonBuilderCustomizer or
   * {@link TypeAdapterFactory} and this low level module will pick them all up and use them to
   * configure the {@link GsonBuilder} it uses to create this {@link Gson} bean. <img
   * src="doc-files/GsonConfig.png"></img>
   *
   * @param customizers All beans of type {@link GsonBuilderCustomizer} will be injected here
   * @param typeAdapterFactories All beans of type {@link TypeAdapterFactory} will be injected here
   * @return Gson instance, configured using the customizers, and with type adapter factories
   *     registered
   */
  @Bean
  public Gson gson(
      List<GsonBuilderCustomizer> customizers, List<TypeAdapterFactory> typeAdapterFactories) {
    var builder = new GsonBuilder();
    typeAdapterFactories.forEach(builder::registerTypeAdapterFactory);
    customizers.forEach(customizer -> customizer.customize(builder));
    return builder.create();
  }

  /** @return TypeAdapterFactory to (de)serialize generic {@link java.util.Optional}s */
  @Bean
  public TypeAdapterFactory optionalTypeAdapterFactory() {
    return OptionalTypeFactory.forJDK();
  }

  /** @return TypeAdapterFactory to (de)serialize autovalue classes */
  @Bean
  public TypeAdapterFactory autoValueTypeAdapterFactory() {
    return new AutoValueTypeAdapterFactory();
  }

  /** @return GsonBuilderCustomizer to register java time {@link com.google.gson.TypeAdapter}s. */
  @Bean
  public GsonBuilderCustomizer javaTimeGsonCustomizer() {
    return Converters::registerAll;
  }

  /** @return GsonBuilderCustomizer to register type adapters for guava immutable collections */
  @Bean
  public GsonBuilderCustomizer immutableCollectionGsonCustomizer() {
    return builder -> {
      builder.registerTypeHierarchyAdapter(ImmutableList.class, new ImmutableListDeserializer());
      builder.registerTypeHierarchyAdapter(ImmutableSet.class, new ImmutableSetDeserializer());
      builder.registerTypeHierarchyAdapter(ImmutableMap.class, new ImmutableMapDeserializer());
    };
  }

  /** @return GsonBuilderCustomizer to disable html escaping */
  @Bean
  public GsonBuilderCustomizer disableHtmlEscapingGsonCustomizer() {
    return GsonBuilder::disableHtmlEscaping;
  }
}
