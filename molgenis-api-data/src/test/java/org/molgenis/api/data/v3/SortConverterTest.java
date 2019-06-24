package org.molgenis.api.data.v3;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class SortConverterTest {
  @Test
  public void convertSingleAttrDefault() {
    assertEquals(new SortConverter().convert("attr"), new Sort().on("attr"));
  }

  @Test
  public void convertSingleAttrAsc() {
    assertEquals(new SortConverter().convert("+attr"), new Sort().on("attr", Sort.Direction.ASC));
  }

  @Test
  public void convertSingleAttrDesc() {
    assertEquals(new SortConverter().convert("-attr"), new Sort().on("attr", Sort.Direction.DESC));
  }

  @Test
  public void convertMultiAttrDefault() {
    assertEquals(new SortConverter().convert("attr0,attr1"), new Sort().on("attr0").on("attr1"));
  }

  @Test
  public void convertMultiAttrAsc() {
    assertEquals(
        new SortConverter().convert("+attr0,+attr1"),
        new Sort().on("attr0", Sort.Direction.ASC).on("attr1", Sort.Direction.ASC));
  }

  @Test
  public void convertMultiAttrDesc() {
    assertEquals(
        new SortConverter().convert("-attr0,-attr1"),
        new Sort().on("attr0", Sort.Direction.DESC).on("attr1", Sort.Direction.DESC));
  }

  @Test
  public void convertMultiAttrAscAndDesc() {
    assertEquals(
        new SortConverter().convert("+attr0,-attr1"),
        new Sort().on("attr0", Sort.Direction.ASC).on("attr1", Sort.Direction.DESC));
  }
}
