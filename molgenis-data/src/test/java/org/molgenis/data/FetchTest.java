package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Sets;
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
    assertFalse(new Fetch().field("field0").equals(new Fetch().field("field1")));
  }

  @Test
  void equalsSubFetchTrue() {
    String field = "field";
    Fetch subFetch = new Fetch();
    assertTrue(new Fetch().field(field, subFetch).equals(new Fetch().field(field, subFetch)));
  }

  @Test
  void equalsSubFetchFalse() {
    String field = "field";
    Fetch subFetch = new Fetch();
    assertFalse(new Fetch().field(field, subFetch).equals(new Fetch().field(field)));
  }

  @Test
  void getFetch() {
    String field = "field";
    Fetch subFetch = new Fetch();
    assertEquals(subFetch, new Fetch().field(field, subFetch).getFetch(field));
  }

  @Test
  void getFields() {
    String field0 = "field0";
    String field1 = "field1";
    String field2 = "field2";
    Fetch fetch = new Fetch().field(field0).field(field1).field(field2);

    assertEquals(Sets.newHashSet(field0, field1, field2), fetch.getFields());
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
