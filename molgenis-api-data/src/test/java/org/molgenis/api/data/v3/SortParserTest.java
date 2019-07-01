package org.molgenis.api.data.v3;

import static org.testng.Assert.assertEquals;

import org.molgenis.api.data.v3.Sort.Direction;
import org.testng.annotations.Test;

public class SortParserTest {

  @Test
  public void testSort() throws ParseException {
    assertEquals(new SortParser("+abc").parse(), new Sort().on("abc", Direction.ASC));
  }

  @Test
  public void testSortminus() throws ParseException {
    assertEquals(new SortParser("-abc").parse(), new Sort().on("abc", Direction.DESC));
  }

  @Test
  public void testSortNoPrefix() throws ParseException {
    assertEquals(new SortParser("abc").parse(), new Sort().on("abc", Direction.ASC));
  }

  @Test
  public void testSortPrefixCombi1() throws ParseException {
    assertEquals(
        new SortParser("+abc,-def,+ghi").parse(),
        new Sort().on("abc", Direction.ASC).on("def", Direction.DESC).on("ghi", Direction.ASC));
  }

  @Test
  public void testSortPrefixCombi2() throws ParseException {
    assertEquals(
        new SortParser("+abc,def,+ghi").parse(),
        new Sort().on("abc", Direction.ASC).on("def", Direction.ASC).on("ghi", Direction.ASC));
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

  // FIXME: anyway to cicumvent this exception and throw a ParseException instead
  // this is caused by the fact that "" is allowed as sort prefix, and in will consume zero
  // characters
  @Test(expectedExceptions = TokenMgrException.class)
  public void testSortillegal5() throws ParseException {
    new SortParser("*abc").parse();
  }
}
