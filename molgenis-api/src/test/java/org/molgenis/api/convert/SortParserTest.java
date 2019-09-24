package org.molgenis.api.convert;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.molgenis.api.model.Sort.Order.Direction.DESC;
import static org.molgenis.api.model.Sort.create;

import org.junit.jupiter.api.Test;
import org.molgenis.api.model.Sort.Order;

class SortParserTest {

  @Test
  void testSortminus() throws ParseException {
    assertEquals(create("abc", DESC), new SortParser("-abc").parse());
  }

  @Test
  void testSortNoPrefix() throws ParseException {
    assertEquals(create("abc"), new SortParser("abc").parse());
  }

  @Test
  void testSortPrefixCombi1() throws ParseException {
    assertEquals(
        create(asList(Order.create("abc"), Order.create("def", DESC), Order.create("ghi"))),
        new SortParser("abc,-def,ghi").parse());
  }

  @Test
  void testSortPrefixCombi2() throws ParseException {
    assertEquals(
        create(asList(Order.create("abc"), Order.create("def"), Order.create("ghi"))),
        new SortParser("abc,def,ghi").parse());
  }

  @Test
  void testSortillegal1() {
    assertThrows(ParseException.class, () -> new SortParser("--abc").parse());
  }

  @Test
  void testSortillegal2() {
    assertThrows(ParseException.class, () -> new SortParser("abc,--def,ghi").parse());
  }

  @Test
  void testSortillegal3() {
    assertThrows(ParseException.class, () -> new SortParser("abc,,ghi").parse());
  }

  @Test
  void testSortillegal4() {
    assertThrows(ParseException.class, () -> new SortParser("abc,def,").parse());
  }

  @Test
  void testSortillegal6() {
    assertThrows(ParseException.class, () -> new SortParser(",abc").parse());
  }

  @Test
  void testSortillegal5() {
    assertThrows(TokenMgrException.class, () -> new SortParser("*abc").parse());
  }
}
