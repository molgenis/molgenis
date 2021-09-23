package org.molgenis.gson;

import com.google.gson.GsonBuilder;

@FunctionalInterface
public interface GsonBuilderCustomizer {
  void customize(GsonBuilder gsonBuilder);
}
