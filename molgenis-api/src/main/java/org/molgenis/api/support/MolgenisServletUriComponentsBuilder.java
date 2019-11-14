package org.molgenis.api.support;

import java.util.Map;
import java.util.Map.Entry;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class MolgenisServletUriComponentsBuilder extends ServletUriComponentsBuilder {
  /**
   * Creates a ServletUriComponentsBuilder from the current request with decoded query params. This
   * is needed to prevent double encoding for the query parameters when using
   * ServletUriComponentsBuilder.build(encoded=false)
   * ServletUriComponentsBuilder.build(encoded=true) cannot be used for RSQL since it throws an
   * exception on the use of '='
   */
  // removing cast results in 'unclear varargs or non-varargs' warning
  @SuppressWarnings("RedundantCast")
  public static ServletUriComponentsBuilder fromCurrentRequestDecodedQuery() {
    ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
    Map<String, String[]> params = getCurrentRequest().getParameterMap();
    for (Entry<String, String[]> param : params.entrySet()) {
      builder.replaceQueryParam(param.getKey(), (Object[]) param.getValue());
    }
    return builder;
  }
}
