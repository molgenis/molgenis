package org.molgenis.api.support;

import java.net.URI;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.response.LinksResponse;
import org.springframework.web.util.UriComponentsBuilder;

public class LinksUtils {
  private LinksUtils() {}

  public static LinksResponse createLinksResponse(int number, int size, int total) {
    URI previous = number > 0 ? createEntitiesResponseUri(number - 1) : null;
    URI self = createEntitiesResponseUri();
    URI next = (number * size) + size < total ? createEntitiesResponseUri(number + 1) : null;
    return LinksResponse.create(previous, self, next);
  }

  private static URI createEntitiesResponseUri() {
    return createEntitiesResponseUri(null);
  }

  private static URI createEntitiesResponseUri(@Nullable @CheckForNull Integer pageNumber) {
    UriComponentsBuilder builder =
        MolgenisServletUriComponentsBuilder.fromCurrentRequestDecodedQuery();
    if (pageNumber != null) {
      builder.replaceQueryParam(PageUtils.PAGE_QUERY_PARAMETER_NAME, pageNumber);
    }
    return builder.build().toUri();
  }
}
