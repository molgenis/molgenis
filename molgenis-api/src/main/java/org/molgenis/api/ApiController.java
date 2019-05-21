package org.molgenis.api;

import static java.util.Objects.requireNonNull;

import java.util.regex.Pattern;

/** Base controller for APIs */
public abstract class ApiController {
  private static final Pattern API_ID_PATTERN = Pattern.compile("[a-z]+");

  private final String apiId;
  private final int apiVersion;

  /**
   * @param apiId API identifier (must match pattern [a-z])
   * @param apiVersion API version (must be > 0)
   */
  public ApiController(String apiId, Integer apiVersion) {
    requireNonNull(apiId);
    if (!apiId.isEmpty() && !API_ID_PATTERN.matcher(apiId).matches()) {
      throw new IllegalArgumentException("API identifier must match pattern [a-z]");
    }
    this.apiId = requireNonNull(apiId);

    requireNonNull(apiVersion);
    if (apiVersion <= 0) {
      throw new IllegalArgumentException("API version must be > 0");
    }
    this.apiVersion = apiVersion;
  }

  String getApiId() {
    return apiId;
  }

  int getApiVersion() {
    return apiVersion;
  }
}
