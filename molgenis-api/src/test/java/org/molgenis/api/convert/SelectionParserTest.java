package org.molgenis.api.convert;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.molgenis.api.model.Selection;
import org.testng.annotations.Test;

public class SelectionParserTest {
  @Test
  public void testSingleItem() throws ParseException {
    Map<String, Selection> itemSelections = Collections.singletonMap("abc", null);
    assertEquals(new SelectionParser("abc").parse(), new Selection(itemSelections));
  }

  @Test
  public void testMultipleItems() throws ParseException {
    Map<String, Selection> itemSelections = new HashMap<>();
    itemSelections.put("abc", null);
    itemSelections.put("def", null);
    assertEquals(new SelectionParser("abc,def").parse(), new Selection(itemSelections));
  }

  @Test
  public void testSingleItemSingleSubSelection() throws ParseException {
    Map<String, Selection> subItemSelections = Collections.singletonMap("def", null);
    Map<String, Selection> itemSelections =
        Collections.singletonMap("abc", new Selection(subItemSelections));
    assertEquals(new SelectionParser("abc(def)").parse(), new Selection(itemSelections));
  }

  @Test
  public void testSingleItemMultipleSubSelection() throws ParseException {
    Map<String, Selection> subItemSelections = new HashMap<>();
    subItemSelections.put("def", null);
    subItemSelections.put("ghi", null);
    Map<String, Selection> itemSelections =
        Collections.singletonMap("abc", new Selection(subItemSelections));
    assertEquals(new SelectionParser("abc(def,ghi)").parse(), new Selection(itemSelections));
  }

  @Test
  public void testMultipleItemsSubSelection() throws ParseException {
    Map<String, Selection> itemSelections = new HashMap<>();
    itemSelections.put("abc", new Selection(Collections.singletonMap("def", null)));
    itemSelections.put("ghi", new Selection(Collections.singletonMap("jkl", null)));
    assertEquals(new SelectionParser("abc(def),ghi(jkl)").parse(), new Selection(itemSelections));
  }

  @Test(expectedExceptions = TokenMgrException.class)
  public void testSingleItemIllegalCharacters() throws ParseException {
    new SelectionParser("a/b").parse();
  }

  @Test(expectedExceptions = ParseException.class)
  public void testSingleItemSubSelectionMissingParenthesis() throws ParseException {
    new SelectionParser("abc(def").parse();
  }

  @Test(expectedExceptions = ParseException.class)
  public void testSingleItemSubSelectionTooManyParenthesis() throws ParseException {
    new SelectionParser("abc(def))").parse();
  }

  @Test(expectedExceptions = ParseException.class)
  public void testSingleItemSubSelectionEmptyParenthesis() throws ParseException {
    Map<String, Selection> itemSelections = Collections.singletonMap("abc", null);
    assertEquals(new SelectionParser("abc()").parse(), new Selection(itemSelections));
  }
}
