package org.molgenis.api.convert;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

import org.molgenis.api.model.Sort;
import org.molgenis.api.model.Sort.Order;
import org.molgenis.api.model.Sort.Order.Direction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SortParserTest {

  @Test
  public void testSort() throws ParseException {
    Assert.assertEquals(new SortParser("+abc").parse(), Sort.create("abc", Direction.ASC));
  }

  @Test
  public void testSortminus() throws ParseException {
    assertEquals(new SortParser("-abc").parse(), Sort.create("abc", Direction.DESC));
  }

  @Test
  public void testSortNoPrefix() throws ParseException {
    assertEquals(new SortParser("abc").parse(), Sort.create("abc"));
  }

  @Test
  public void testSortPrefixCombi1() throws ParseException {
    assertEquals(
        new SortParser("+abc,-def,+ghi").parse(),
        Sort.create(
            asList(
                Order.create("abc", Direction.ASC),
                Order.create("def", Direction.DESC),
                Order.create("ghi", Direction.ASC))));
  }

  @Test
  public void testSortPrefixCombi2() throws ParseException {
    assertEquals(
        new SortParser("+abc,def,+ghi").parse(),
        Sort.create(
            asList(
                Order.create("abc", Direction.ASC),
                Order.create("def"),
                Order.create("ghi", Direction.ASC))));
  }

  @Test(expectedExceptions = ParseException.class)
  public void testSortillegal1() throws ParseException {
    new SortParser("++abc").parse();
  }

  @Test(expectedExceptions = ParseException.class)
  public void testSortillegal2() throws ParseException {
    new SortParser("+abc,--def,+ghi").parse();
  }

  @Test(expectedExceptions = ParseException.class)
  public void testSortillegal3() throws ParseException {
    new SortParser("+abc,,+ghi").parse();
  }

  @Test(expectedExceptions = ParseException.class)
  public void testSortillegal4() throws ParseException {
    new SortParser("+abc,+def,").parse();
  }

  @Test(expectedExceptions = ParseException.class)
  public void testSortillegal6() throws ParseException {
    new SortParser(",abc").parse();
  }

  @Test(expectedExceptions = TokenMgrException.class)
  public void testSortillegal5() throws ParseException {
    new SortParser("*abc").parse();
  }
}
