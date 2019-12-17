package org.molgenis.api.support;

import java.net.URI;
import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.LinksResponse.Builder;
import org.springframework.web.util.UriComponentsBuilder;

public class LinksUtils {
  private LinksUtils() {}

  public static LinksResponse createLinksResponse(int number, int size, int total) {
    Builder builder = LinksResponse.builder().setSelf(createEntitiesResponseUri());
    createPreviousUri(number, size, total).ifPresent(builder::setPrevious);
    createNextUri(number, size, total).ifPresent(builder::setNext);
    return builder.build();
  }

  private static Optional<URI> createPreviousUri(int number, int size, int totalElements) {
    Optional<URI> optionalPreviousUri;
    if (number == 0) {
      optionalPreviousUri = Optional.empty();
    } else {
      int totalPages = PageUtils.getTotalPages(size, totalElements);
      if (totalPages == 0) {
        optionalPreviousUri = Optional.empty();
      } else {
        int previousNumber;
        if (number - 1 < totalPages) {
          previousNumber = number - 1;
        } else {
          previousNumber = PageUtils.getTotalPages(size, totalElements) - 1;
        }
        optionalPreviousUri = Optional.of(createEntitiesResponseUri(previousNumber));
      }
    }
    return optionalPreviousUri;
  }

  private static Optional<URI> createNextUri(int number, int size, int total) {
    Optional<URI> optionalNextUri;
    if ((number * size) + size < total) {
      optionalNextUri = Optional.of(createEntitiesResponseUri(number + 1));
    } else {
      optionalNextUri = Optional.empty();
    }
    return optionalNextUri;
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
