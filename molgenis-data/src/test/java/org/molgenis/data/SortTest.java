package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.data.Sort.Direction.ASC;
import static org.molgenis.data.Sort.Direction.DESC;
import static org.molgenis.data.Sort.Order;

import java.util.Arrays;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class SortTest {
  @Test
  void sortSingleAttrDefault() {
    Sort sort = new Sort("attr");
    assertEquals("attr", sort.iterator().next().getAttr());
    assertEquals(ASC, sort.iterator().next().getDirection());
  }

  @Test
  void sortSingleAttrAsc() {
    Sort sort = new Sort("attr", Sort.Direction.ASC);
    assertEquals("attr", sort.iterator().next().getAttr());
    assertEquals(ASC, sort.iterator().next().getDirection());
  }

  @Test
  void sortSingleAttrDesc() {
    Sort sort = new Sort("attr", Sort.Direction.DESC);
    assertEquals("attr", sort.iterator().next().getAttr());
    assertEquals(DESC, sort.iterator().next().getDirection());
  }

  @Test
  void sortSingleAttrBuilderDefault() {
    Sort sort = new Sort().on("attr");
    assertEquals("attr", sort.iterator().next().getAttr());
    assertEquals(ASC, sort.iterator().next().getDirection());
  }

  @Test
  void sortSingleAttrBuilderAsc() {
    Sort sort = new Sort().on("attr", Sort.Direction.ASC);
    assertEquals("attr", sort.iterator().next().getAttr());
    assertEquals(ASC, sort.iterator().next().getDirection());
  }

  @Test
  void sortSingleAttrBuilderDesc() {
    Sort sort = new Sort().on("attr", Sort.Direction.DESC);
    assertEquals("attr", sort.iterator().next().getAttr());
    assertEquals(DESC, sort.iterator().next().getDirection());
  }

  @Test
  void sortMultipleAttrDefault() {
    Sort sort = new Sort(Arrays.asList(new Sort.Order("attr0"), new Sort.Order("attr1")));
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(new Order("attr0"), it.next());
    assertEquals(new Order("attr1"), it.next());
  }

  @Test
  void sortMultipleAttrAsc() {
    Sort sort =
        new Sort(
            Arrays.asList(
                new Sort.Order("attr0", Sort.Direction.ASC),
                new Sort.Order("attr1", Sort.Direction.ASC)));
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(new Order("attr0", ASC), it.next());
    assertEquals(new Order("attr1", ASC), it.next());
  }

  @Test
  void sortMultipleAttrDesc() {
    Sort sort =
        new Sort(
            Arrays.asList(
                new Sort.Order("attr0", Sort.Direction.DESC),
                new Sort.Order("attr1", Sort.Direction.DESC)));
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(new Order("attr0", DESC), it.next());
    assertEquals(new Order("attr1", DESC), it.next());
  }

  @Test
  void sortMultipleAttrAscAndDesc() {
    Sort sort =
        new Sort(
            Arrays.asList(
                new Sort.Order("attr0", Sort.Direction.ASC),
                new Sort.Order("attr1", Sort.Direction.DESC)));
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(new Order("attr0", ASC), it.next());
    assertEquals(new Order("attr1", DESC), it.next());
  }

  @Test
  void sortMultipleAttrBuilderDefault() {
    Sort sort = new Sort().on("attr0").on("attr1");
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(new Order("attr0"), it.next());
    assertEquals(new Order("attr1"), it.next());
  }

  @Test
  void sortMultipleAttrBuilderAsc() {
    Sort sort = new Sort().on("attr0", Sort.Direction.ASC).on("attr1", Sort.Direction.ASC);
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(new Order("attr0", ASC), it.next());
    assertEquals(new Order("attr1", ASC), it.next());
  }

  @Test
  void sortMultipleAttrBuilderDesc() {
    Sort sort = new Sort().on("attr0", Sort.Direction.DESC).on("attr1", Sort.Direction.DESC);
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(new Order("attr0", DESC), it.next());
    assertEquals(new Order("attr1", DESC), it.next());
  }

  @Test
  void sortMultipleAttrBuilderAscAndDesc() {
    Sort sort = new Sort().on("attr0", Sort.Direction.ASC).on("attr1", Sort.Direction.DESC);
    Iterator<Sort.Order> it = sort.iterator();
    assertEquals(new Order("attr0", ASC), it.next());
    assertEquals(new Order("attr1", DESC), it.next());
  }
}
