package org.molgenis.web.support;

import java.util.Map;
import java.util.Map.Entry;

public class DecodingServletUriComponentsBuilder
    extends org.springframework.web.servlet.support.ServletUriComponentsBuilder {
  /**
   * Creates a ServletUriComponentsBuilder from the current request with decoded query params. This
   * is needed to prevent double encoding for the query parameters when using
   * ServletUriComponentsBuilder.build(encoded=false)
   * ServletUriComponentsBuilder.build(encoded=true) cannot be used for RSQL since it throws an
   * exception on the use of '='
   */
  public static org.springframework.web.servlet.support.ServletUriComponentsBuilder
      fromCurrentRequestDecodedQuery() {
    org.springframework.web.servlet.support.ServletUriComponentsBuilder builder =
        org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest();
    Map<String, String[]> params = getCurrentRequest().getParameterMap();
    for (Entry<String, String[]> param : params.entrySet()) {
      builder.replaceQueryParam(param.getKey(), param.getValue());
    }
    return builder;
  }
}
