package org.molgenis.dataexplorer.negotiator;

import static java.util.Collections.emptyList;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ExportValidationResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ExportValidationResponse {
  public abstract boolean isValid();

  public abstract String message();

  public abstract List<String> enabledCollections();

  public abstract List<String> disabledCollections();

  public static ExportValidationResponse create(boolean success, String message) {
    return new AutoValue_ExportValidationResponse(success, message, emptyList(), emptyList());
  }

  public static ExportValidationResponse create(
      boolean success,
      String message,
      List<String> enabledCollections,
      List<String> disabledCollections) {
    return new AutoValue_ExportValidationResponse(
        success, message, enabledCollections, disabledCollections);
  }
}
