package org.molgenis.api.convert;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.molgenis.api.model.Sort;
import org.molgenis.api.model.Sort.Order;
import org.molgenis.api.model.Sort.Order.Direction;

class SortParserTest {

  @Test
  void testSortminus() throws ParseException {
    assertEquals(new SortParser("-abc").parse(), Sort.create("abc", Direction.DESC));
  }

  @Test
  void testSortNoPrefix() throws ParseException {
    assertEquals(new SortParser("abc").parse(), Sort.create("abc"));
  }

  @Test
  void testSortPrefixCombi1() throws ParseException {
    assertEquals(
        new SortParser("abc,-def,ghi").parse(),
        Sort.create(
            asList(Order.create("abc"), Order.create("def", Direction.DESC), Order.create("ghi"))));
  }

  @Test
  void testSortPrefixCombi2() throws ParseException {
    assertEquals(
        new SortParser("abc,def,ghi").parse(),
        Sort.create(asList(Order.create("abc"), Order.create("def"), Order.create("ghi"))));
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
