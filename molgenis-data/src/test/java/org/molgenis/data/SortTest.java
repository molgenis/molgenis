package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class SortTest {
  @Test
  void sortSingleAttrDefault() {
    Sort sort = new Sort("attr");
    assertEquals(sort.iterator().next().getAttr(), "attr");
    assertEquals(sort.iterator().next().getDirection(), Sort.Direction.ASC);
  }

  @Test
  void sortSingleAttrAsc() {
    Sort sort = new Sort("attr", Sort.Direction.ASC);
    assertEquals(sort.iterator().next().getAttr(), "attr");
    assertEquals(sort.iterator().next().getDirection(), Sort.Direction.ASC);
  }

  @Test
  void sortSingleAttrDesc() {
    Sort sort = new Sort("attr", Sort.Direction.DESC);
    assertEquals(sort.iterator().next().getAttr(), "attr");
    assertEquals(sort.iterator().next().getDirection(), Sort.Direction.DESC);
  }

  @Test
  void sortSingleAttrBuilderDefault() {
    Sort sort = new Sort().on("attr");
    assertEquals(sort.iterator().next().getAttr(), "attr");
    assertEquals(sort.iterator().next().getDirection(), Sort.Direction.ASC);
  }

  @Test
  void sortSingleAttrBuilderAsc() {
    Sort sort = new Sort().on("attr", Sort.Direction.ASC);
    assertEquals(sort.iterator().next().getAttr(), "attr");
    assertEquals(sort.iterator().next().getDirection(), Sort.Direction.ASC);
  }

  @Test
  void sortSingleAttrBuilderDesc() {
    Sort sort = new Sort().on("attr", Sort.Direction.DESC);
    assertEquals(sort.iterator().next().getAttr(), "attr");
    assertEquals(sort.iterator().next().getDirection(), Sort.Direction.DESC);
  }

  @Test
  void sortMultipleAttrDefault() {
    Sort sort = new Sort(Arrays.asList(new Sort.Order("attr0"), new Sort.Order("attr1")));
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(it.next(), new Sort.Order("attr0"));
    assertEquals(it.next(), new Sort.Order("attr1"));
  }

  @Test
  void sortMultipleAttrAsc() {
    Sort sort =
        new Sort(
            Arrays.asList(
                new Sort.Order("attr0", Sort.Direction.ASC),
                new Sort.Order("attr1", Sort.Direction.ASC)));
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(it.next(), new Sort.Order("attr0", Sort.Direction.ASC));
    assertEquals(it.next(), new Sort.Order("attr1", Sort.Direction.ASC));
  }

  @Test
  void sortMultipleAttrDesc() {
    Sort sort =
        new Sort(
            Arrays.asList(
                new Sort.Order("attr0", Sort.Direction.DESC),
                new Sort.Order("attr1", Sort.Direction.DESC)));
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(it.next(), new Sort.Order("attr0", Sort.Direction.DESC));
    assertEquals(it.next(), new Sort.Order("attr1", Sort.Direction.DESC));
  }

  @Test
  void sortMultipleAttrAscAndDesc() {
    Sort sort =
        new Sort(
            Arrays.asList(
                new Sort.Order("attr0", Sort.Direction.ASC),
                new Sort.Order("attr1", Sort.Direction.DESC)));
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(it.next(), new Sort.Order("attr0", Sort.Direction.ASC));
    assertEquals(it.next(), new Sort.Order("attr1", Sort.Direction.DESC));
  }

  @Test
  void sortMultipleAttrBuilderDefault() {
    Sort sort = new Sort().on("attr0").on("attr1");
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(it.next(), new Sort.Order("attr0"));
    assertEquals(it.next(), new Sort.Order("attr1"));
  }

  @Test
  void sortMultipleAttrBuilderAsc() {
    Sort sort = new Sort().on("attr0", Sort.Direction.ASC).on("attr1", Sort.Direction.ASC);
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(it.next(), new Sort.Order("attr0", Sort.Direction.ASC));
    assertEquals(it.next(), new Sort.Order("attr1", Sort.Direction.ASC));
  }

  @Test
  void sortMultipleAttrBuilderDesc() {
    Sort sort = new Sort().on("attr0", Sort.Direction.DESC).on("attr1", Sort.Direction.DESC);
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(it.next(), new Sort.Order("attr0", Sort.Direction.DESC));
    assertEquals(it.next(), new Sort.Order("attr1", Sort.Direction.DESC));
  }

  @Test
  void sortMultipleAttrBuilderAscAndDesc() {
    Sort sort = new Sort().on("attr0", Sort.Direction.ASC).on("attr1", Sort.Direction.DESC);
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(it.next(), new Sort.Order("attr0", Sort.Direction.ASC));
    assertEquals(it.next(), new Sort.Order("attr1", Sort.Direction.DESC));
  }
}
