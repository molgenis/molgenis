package org.molgenis.api.permissions;

import static java.lang.Math.ceil;

import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.model.response.PageResponse;
import org.molgenis.api.model.response.PagedApiResponse;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

class PermissionResponseUtils {

  private PermissionResponseUtils() {}

  static PagedApiResponse getPermissionResponse(
      String query, int page, int pageSize, int totalItems, Object data) {
    PageResponse pageResponse =
        PageResponse.create(pageSize, totalItems, getTotalPages(pageSize, totalItems), page);
    LinksResponse linksResponse = getLinks(query, page, pageSize, totalItems);
    return PagedApiResponse.create(pageResponse, linksResponse, data);
  }

  static PagedApiResponse getPermissionResponse(String query, Object data) {
    LinksResponse linksResponse = getLinks(query, null, null, null);
    return PagedApiResponse.create(null, linksResponse, data);
  }

  private static LinksResponse getLinks(
      String query, Integer page, Integer pageSize, Integer totalItems) {
    URI nextUri = null;
    URI selfUri =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .query(getQueryString(query, null, null))
            .build()
            .toUri();
    URI previousUri = null;
    if (page != null) {
      selfUri =
          ServletUriComponentsBuilder.fromCurrentRequestUri()
              .query(getQueryString(query, page, pageSize))
              .build()
              .toUri();
      if (page > 1) {
        previousUri =
            ServletUriComponentsBuilder.fromCurrentRequestUri()
                .query(getQueryString(query, page - 1, pageSize))
                .build()
                .toUri();
      }
      if (page * pageSize < totalItems) {
        nextUri =
            ServletUriComponentsBuilder.fromCurrentRequestUri()
                .query(getQueryString(query, page + 1, pageSize))
                .build()
                .toUri();
      }
    }

    return LinksResponse.create(previousUri, selfUri, nextUri);
  }

  private static String getQueryString(String query, Integer page, Integer pageSize) {
    StringBuilder queryStringBuffer = new StringBuilder();
    if (StringUtils.isNotEmpty(query)) {
      queryStringBuffer.append("q=").append(query);
    }
    if (page != null) {
      if (StringUtils.isNotEmpty(query)) {
        queryStringBuffer.append("&");
      }
      queryStringBuffer.append("page=").append(page).append("&pageSize=").append(pageSize);
    }
    return queryStringBuffer.toString();
  }

  private static int getTotalPages(int pageSize, int totalItems) {
    return (int) ceil((double) totalItems / (double) pageSize);
  }
}
