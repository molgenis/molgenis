package org.molgenis.api.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.data.Sort.Direction.ASC;
import static org.molgenis.data.Sort.Direction.DESC;

import org.junit.jupiter.api.Test;
import org.molgenis.data.Sort;

class SortConverterTest {
  @Test
  void convertSingleAttrDefault() {
    assertEquals(new Sort().on("attr"), new SortConverter().convert("attr"));
  }

  @Test
  void convertSingleAttrAsc() {
    assertEquals(new Sort().on("attr", ASC), new SortConverter().convert("attr:asc"));
  }

  @Test
  void convertSingleAttrDesc() {
    assertEquals(new Sort().on("attr", DESC), new SortConverter().convert("attr:desc"));
  }

  @Test
  void convertMultiAttrDefault() {
    assertEquals(new Sort().on("attr0").on("attr1"), new SortConverter().convert("attr0,attr1"));
  }

  @Test
  void convertMultiAttrAsc() {
    assertEquals(
        new Sort().on("attr0", ASC).on("attr1", ASC),
        new SortConverter().convert("attr0:asc,attr1:asc"));
  }

  @Test
  void convertMultiAttrDesc() {
    assertEquals(
        new Sort().on("attr0", DESC).on("attr1", DESC),
        new SortConverter().convert("attr0:desc,attr1:desc"));
  }

  @Test
  void convertMultiAttrAscAndDesc() {
    assertEquals(
        new Sort().on("attr0", ASC).on("attr1", DESC),
        new SortConverter().convert("attr0:asc,attr1:desc"));
  }
}
