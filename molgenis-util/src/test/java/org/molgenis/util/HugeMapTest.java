package org.molgenis.util;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.util.HugeMap.THRESHOLD;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HugeMapTest {
  private HugeMap<String, String> hugeMap;

  @BeforeEach
  void beforeMethod() {
    hugeMap = new HugeMap<>();
  }

  @AfterEach
  void afterMethod() throws IOException {
    hugeMap.close();
  }

  @Test
  void clear() {
    hugeMap.put("key", "value");
    hugeMap.clear();
    assertTrue(hugeMap.isEmpty());
  }

  @Test
  void clearLarge() {
    fillToThreshold();
    hugeMap.clear();
    assertTrue(hugeMap.isEmpty());
  }

  @Test
  void containsKey() {
    hugeMap.put("key", "value");
    assertTrue(hugeMap.containsKey("key"));
    assertFalse(hugeMap.containsKey("value"));
  }

  @Test
  void containsKeyLarge() {
    fillToThreshold();
    assertTrue(hugeMap.containsKey("3"));
    assertFalse(hugeMap.containsKey("value"));
  }

  @Test
  void containsValue() {
    hugeMap.put("key", "value");
    assertTrue(hugeMap.containsValue("value"));
    assertFalse(hugeMap.containsValue("key"));
  }

  @Test
  void containsValueLarge() {
    fillToThreshold();
    assertTrue(hugeMap.containsValue("3"));
    assertFalse(hugeMap.containsValue("key"));
  }

  @Test
  void entrySet() {
    hugeMap.put("key", "value");
    assertEquals(1, hugeMap.entrySet().size());
  }

  @Test
  void entrySetLarge() {
    fillToThreshold();
    assertEquals(THRESHOLD, hugeMap.entrySet().size());
  }

  @Test
  void get() {
    hugeMap.put("key", "value");
    assertEquals("value", hugeMap.get("key"));
    assertNull(hugeMap.get("value"));
  }

  @Test
  void getLarge() {
    fillToThreshold();
    assertEquals("2", hugeMap.get("2"));
    assertNull(hugeMap.get("value"));
  }

  @Test
  void isEmpty() {
    assertTrue(hugeMap.isEmpty());
    hugeMap.put("key", "value");
    assertFalse(hugeMap.isEmpty());
  }

  @Test
  void isEmptyLarge() {
    assertTrue(hugeMap.isEmpty());
    fillToThreshold();
    assertFalse(hugeMap.isEmpty());
  }

  @Test
  void keySet() {
    hugeMap.put("key", "value");
    assertEquals(newHashSet("key"), hugeMap.keySet());
  }

  @Test
  void keySetLarge() {
    fillToThreshold();
    assertEquals(THRESHOLD, hugeMap.keySet().size());
  }

  @Test
  void putAll() {
    hugeMap.putAll(Collections.singletonMap("key", "value"));
    assertEquals(1, hugeMap.size());
  }

  @Test
  void putAllLarge() {
    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < HugeMap.THRESHOLD; i++) {
      map.put(Integer.toString(i), Integer.toString(i));
    }

    hugeMap.putAll(map);
    assertEquals(THRESHOLD, hugeMap.size());
  }

  @Test
  void remove() {
    fillToThreshold();
    assertEquals("5", hugeMap.remove("5"));
    assertEquals(THRESHOLD - 1, hugeMap.size());
  }

  @Test
  void removeLarge() {
    hugeMap.put("test", "value");
    assertEquals("value", hugeMap.remove("test"));
    assertTrue(hugeMap.isEmpty());
  }

  @Test
  void values() {
    hugeMap.put("key", "value");
    assertEquals(1, hugeMap.values().size());
  }

  @Test
  void valuesLarge() {
    fillToThreshold();
    assertEquals(THRESHOLD, hugeMap.values().size());
  }

  private void fillToThreshold() {
    IntStream.range(0, HugeMap.THRESHOLD)
        .mapToObj(Integer::toString)
        .forEach(s -> hugeMap.put(s, s));
  }
}
