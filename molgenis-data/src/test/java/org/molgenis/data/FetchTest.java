package org.molgenis.data;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.Map.Entry;
import org.junit.jupiter.api.Test;

class FetchTest {
  @Test
  void equalsTrue() {
    String field = "field";
    assertEquals(new Fetch().field(field), new Fetch().field(field));
  }

  @Test
  void equalsFalse() {
    assertNotEquals(new Fetch().field("field0"), new Fetch().field("field1"));
  }

  @Test
  void equalsSubFetchTrue() {
    String field = "field";
    Fetch subFetch = new Fetch();
    assertEquals(new Fetch().field(field, subFetch), new Fetch().field(field, subFetch));
  }

  @Test
  void equalsSubFetchFalse() {
    String field = "field";
    Fetch subFetch = new Fetch();
    assertNotEquals(new Fetch().field(field, subFetch), new Fetch().field(field));
  }

  @Test
  void getFetch() {
    String field = "field";
    Fetch subFetch = new Fetch();
    assertEquals(new Fetch().field(field, subFetch).getFetch(field), subFetch);
  }

  @Test
  void getFields() {
    String field0 = "field0";
    String field1 = "field1";
    String field2 = "field2";
    Fetch fetch = new Fetch().field(field0).field(field1).field(field2);

    assertEquals(fetch.getFields(), newHashSet(field0, field1, field2));
  }

  @Test
  void hasFieldTrue() {
    String field = "field";
    assertTrue(new Fetch().field(field).hasField(field));
  }

  @Test
  void hasFieldFalse() {
    String field = "field";
    assertFalse(new Fetch().hasField(field));
  }

  @Test
  void iterator() {
    String field0 = "field0";
    String field1 = "field1";
    String field2 = "field2";
    Fetch fetch = new Fetch().field(field0).field(field1).field(field2);

    Iterator<Entry<String, Fetch>> it = fetch.iterator();
    assertTrue(it.hasNext());
    assertEquals("field0", it.next().getKey());
    assertTrue(it.hasNext());
    assertEquals("field1", it.next().getKey());
    assertTrue(it.hasNext());
    assertEquals("field2", it.next().getKey());
    assertFalse(it.hasNext());
  }
}
