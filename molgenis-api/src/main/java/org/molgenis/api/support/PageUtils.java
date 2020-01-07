package org.molgenis.api.support;

import org.molgenis.api.model.response.PageResponse;

public class PageUtils {
  public static final String PAGE_QUERY_PARAMETER_NAME = "page";

  private PageUtils() {}

  public static PageResponse getPageResponse(int pageSize, int offset, int total) {
    int newNumber = offset / pageSize;
    return PageResponse.create(pageSize, total, newNumber);
  }

  public static int getTotalPages(int pageSize, int totalElements) {
    return pageSize > 0 ? (int) Math.ceil(totalElements / (double) pageSize) : 0;
  }
}
