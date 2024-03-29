package org.molgenis.api.model.response;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ApiResponse.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ApiResponse {

  public abstract Object getData();

  public static ApiResponse create(Object data) {
    return new AutoValue_ApiResponse(data);
  }
}
