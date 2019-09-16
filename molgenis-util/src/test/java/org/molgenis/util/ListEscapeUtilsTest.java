package org.molgenis.util;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.molgenis.util.ListEscapeUtils.toList;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class ListEscapeUtilsTest {

  @Test
  void toListString() {
    assertEquals(singletonList("a"), toList("a"));
    assertEquals(asList("a", "b", "c"), toList("a,b,c"));

    assertEquals(singletonList(","), toList("\\,"));
    assertEquals(singletonList("a,b"), toList("a\\,b"));

    assertEquals(singletonList("\\"), toList("\\\\"));
    assertEquals(singletonList("a\\b"), toList("a\\\\b"));

    assertEquals(asList("a", "b", ""), toList("a,b,"));
    assertEquals(asList("a", "", "c"), toList("a,,c"));
    assertEquals(asList("", "b", "c"), toList(",b,c"));

    assertEquals(emptyList(), toList(""));
    assertNull(ListEscapeUtils.toList(null));
  }

  @Test
  void toListStringcharchar() {
    assertEquals("a,b,c", ListEscapeUtils.toString(asList("a", "b", "c"), ',', '/'));
    assertEquals("a/,b/,c", ListEscapeUtils.toString(singletonList("a,b,c"), ',', '/'));
    assertEquals("//", ListEscapeUtils.toString(singletonList("/"), ',', '/'));
  }

  @Test
  void toListStringcharchar_exception() {
    assertThrows(IllegalArgumentException.class, () -> ListEscapeUtils.toList("", 'a', 'a'));
  }

  @Test
  void toStringList() {
    assertEquals(ListEscapeUtils.toString(singletonList("a")), "a");
    assertEquals(ListEscapeUtils.toString(asList("a", "b", "c")), "a,b,c");

    assertEquals(ListEscapeUtils.toString(singletonList(",")), "\\,");
    assertEquals(ListEscapeUtils.toString(singletonList("a,b")), "a\\,b");

    assertEquals(ListEscapeUtils.toString(singletonList("\\")), "\\\\");
    assertEquals(ListEscapeUtils.toString(singletonList("a\\b")), "a\\\\b");

    assertEquals(ListEscapeUtils.toString(asList("a", "b", "")), "a,b,");
    assertEquals(ListEscapeUtils.toString(asList("a", "", "c")), "a,,c");
    assertEquals(ListEscapeUtils.toString(asList("", "b", "c")), ",b,c");

    assertEquals(ListEscapeUtils.toString(asList("a", "b", null)), "a,b,");
    assertEquals(ListEscapeUtils.toString(asList("a", null, "c")), "a,,c");
    assertEquals(ListEscapeUtils.toString(asList(null, "b", "c")), ",b,c");

    assertEquals(ListEscapeUtils.toString(emptyList()), "");
    assertEquals(ListEscapeUtils.toString(null), null);
  }

  @Test
  void toStringListcharchar() {
    assertEquals(ListEscapeUtils.toString(asList("a", "b", "c"), ',', '/'), "a,b,c");
    assertEquals(ListEscapeUtils.toString(singletonList("a,b,c"), ',', '/'), "a/,b/,c");
    assertEquals(ListEscapeUtils.toString(singletonList("/"), ',', '/'), "//");
  }

  @Test
  void toStringListcharchar_exception() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ListEscapeUtils.toString(Collections.emptyList(), 'a', 'a'));
  }
}
