package org.molgenis.api.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.molgenis.api.model.Selection;

class SelectionParserTest {
  @Test
  void testSingleItem() throws ParseException {
    Map<String, Selection> itemSelections = Collections.singletonMap("abc", null);
    assertEquals(new Selection(itemSelections), new SelectionParser("abc").parse());
  }

  @Test
  void testMultipleItems() throws ParseException {
    Map<String, Selection> itemSelections = new HashMap<>();
    itemSelections.put("abc", null);
    itemSelections.put("def", null);
    assertEquals(new Selection(itemSelections), new SelectionParser("abc,def").parse());
  }

  @Test
  void testSingleItemSingleSubSelection() throws ParseException {
    Map<String, Selection> subItemSelections = Collections.singletonMap("def", null);
    Map<String, Selection> itemSelections =
        Collections.singletonMap("abc", new Selection(subItemSelections));
    assertEquals(new Selection(itemSelections), new SelectionParser("abc(def)").parse());
  }

  @Test
  void testSingleItemMultipleSubSelection() throws ParseException {
    Map<String, Selection> subItemSelections = new HashMap<>();
    subItemSelections.put("def", null);
    subItemSelections.put("ghi", null);
    Map<String, Selection> itemSelections =
        Collections.singletonMap("abc", new Selection(subItemSelections));
    assertEquals(new Selection(itemSelections), new SelectionParser("abc(def,ghi)").parse());
  }

  @Test
  void testMultipleItemsSubSelection() throws ParseException {
    Map<String, Selection> itemSelections = new HashMap<>();
    itemSelections.put("abc", new Selection(Collections.singletonMap("def", null)));
    itemSelections.put("ghi", new Selection(Collections.singletonMap("jkl", null)));
    assertEquals(new Selection(itemSelections), new SelectionParser("abc(def),ghi(jkl)").parse());
  }

  @Test
  void testSingleItemIllegalCharacters() {
    assertThrows(TokenMgrException.class, () -> new SelectionParser("a/b").parse());
  }

  @Test
  void testSingleItemSubSelectionMissingParenthesis() {
    assertThrows(ParseException.class, () -> new SelectionParser("abc(def").parse());
  }

  @Test
  void testSingleItemSubSelectionTooManyParenthesis() {
    assertThrows(ParseException.class, () -> new SelectionParser("abc(def))").parse());
  }

  @Test
  void testSingleItemSubSelectionEmptyParenthesis() {
    assertThrows(ParseException.class, () -> new SelectionParser("abc()").parse());
  }
}
