package org.molgenis.api.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.molgenis.api.model.response.PageResponse;

class PageUtilsTest {
  @Test
  void getPageResponse() {
    PageResponse pageResponse =
        PageResponse.builder()
            .setSize(10)
            .setNumber(0)
            .setTotalPages(9)
            .setTotalElements(90)
            .build();
    assertEquals(pageResponse, PageUtils.getPageResponse(10, 0, 90));
  }

  @Test
  void getPageResponseTotalPagesRounding() {
    PageResponse pageResponse =
        PageResponse.builder()
            .setSize(10)
            .setNumber(0)
            .setTotalPages(9)
            .setTotalElements(89)
            .build();
    assertEquals(pageResponse, PageUtils.getPageResponse(10, 0, 89));
  }

  @Test
  void testGetTotalPages() {
    assertEquals(2, PageUtils.getTotalPages(5, 10));
  }

  @Test
  void testGetTotalPagesBelow() {
    assertEquals(2, PageUtils.getTotalPages(5, 9));
  }

  @Test
  void testGetTotalPagesAbove() {
    assertEquals(3, PageUtils.getTotalPages(5, 11));
  }

  @Test
  void testGetTotalPagesZeroPageSize() {
    assertEquals(0, PageUtils.getTotalPages(0, 10));
  }
}
