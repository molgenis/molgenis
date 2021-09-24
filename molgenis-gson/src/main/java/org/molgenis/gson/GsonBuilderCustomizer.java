package org.molgenis.gson;

import com.google.gson.GsonBuilder;

/**
 * Create beans of this type to configure the {@link GsonBuilder} used to create the {@link
 * com.google.gson.Gson} bean.
 */
@FunctionalInterface
public interface GsonBuilderCustomizer {
  void customize(GsonBuilder gsonBuilder);
}
