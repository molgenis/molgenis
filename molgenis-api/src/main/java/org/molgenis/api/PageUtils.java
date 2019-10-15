package org.molgenis.api;

import org.molgenis.api.model.response.PageResponse;

public class PageUtils {

  private PageUtils() {}

  public static PageResponse getPageResponse(int size, int offset, int total, int pageSize) {
    return PageResponse.create(
        size, total, total > 0 ? (int) Math.ceil(total / (double) pageSize) : 0, offset / pageSize);
  }
}
