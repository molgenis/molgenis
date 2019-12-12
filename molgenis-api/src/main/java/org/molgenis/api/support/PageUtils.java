package org.molgenis.api.support;

import org.molgenis.api.model.response.PageResponse;

public class PageUtils {
  public static final String PAGE_QUERY_PARAMETER_NAME = "page";

  private PageUtils() {}

  public static PageResponse getPageResponse(int size, int offset, int total, int pageSize) {
    int newNumber = offset / pageSize;
    return PageResponse.create(size, total, newNumber);
  }
}
