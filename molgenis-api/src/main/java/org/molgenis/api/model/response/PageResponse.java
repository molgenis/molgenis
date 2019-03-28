package org.molgenis.api.model.response;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_PageResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class PageResponse {
  public abstract int getSize();

  public abstract int getTotalElements();

  public abstract int getTotalPages();

  public abstract int getNumber();

  public static PageResponse create(int size, int totalElements, int totalPages, int number) {
    return new AutoValue_PageResponse(size, totalElements, totalPages, number);
  }
}
