package org.molgenis.api.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.molgenis.data.Sort;

class SortConverterTest {
  @Test
  void convertSingleAttrDefault() {
    assertEquals(new SortConverter().convert("attr"), new Sort().on("attr"));
  }

  @Test
  void convertSingleAttrAsc() {
    assertEquals(
        new SortConverter().convert("attr:asc"), new Sort().on("attr", Sort.Direction.ASC));
  }

  @Test
  void convertSingleAttrDesc() {
    assertEquals(
        new SortConverter().convert("attr:desc"), new Sort().on("attr", Sort.Direction.DESC));
  }

  @Test
  void convertMultiAttrDefault() {
    assertEquals(new SortConverter().convert("attr0,attr1"), new Sort().on("attr0").on("attr1"));
  }

  @Test
  void convertMultiAttrAsc() {
    assertEquals(
        new SortConverter().convert("attr0:asc,attr1:asc"),
        new Sort().on("attr0", Sort.Direction.ASC).on("attr1", Sort.Direction.ASC));
  }

  @Test
  void convertMultiAttrDesc() {
    assertEquals(
        new SortConverter().convert("attr0:desc,attr1:desc"),
        new Sort().on("attr0", Sort.Direction.DESC).on("attr1", Sort.Direction.DESC));
  }

  @Test
  void convertMultiAttrAscAndDesc() {
    assertEquals(
        new SortConverter().convert("attr0:asc,attr1:desc"),
        new Sort().on("attr0", Sort.Direction.ASC).on("attr1", Sort.Direction.DESC));
  }
}
