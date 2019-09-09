package org.molgenis.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Iterators;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HugeSetTest {
  private HugeSet<String> hugeSet;

  @BeforeEach
  void beforeMethod() {
    hugeSet = new HugeSet<>();
  }

  @AfterEach
  void afterMethod() throws IOException {
    hugeSet.close();
  }

  @Test
  void add() {
    hugeSet.add("test");
    assertEquals(hugeSet.size(), 1);
  }

  @Test
  void addLarge() {
    fillToThreshold();
    assertEquals(hugeSet.size(), HugeSet.THRESHOLD);
  }

  @Test
  void clear() {
    List<String> contents = Arrays.asList("test1", "test2", "test3");
    hugeSet.addAll(contents);
    hugeSet.clear();
    assertTrue(hugeSet.isEmpty());
  }

  @Test
  void clearLarge() {
    fillToThreshold();
    hugeSet.clear();
    assertTrue(hugeSet.isEmpty());
  }

  @Test
  void contains() {
    List<String> contents = Arrays.asList("test1", "test2", "test3");
    hugeSet.addAll(contents);
    assertTrue(hugeSet.contains("test2"));
    assertFalse(hugeSet.contains("test4"));
  }

  @Test
  void containsLarge() {
    fillToThreshold();
    assertTrue(hugeSet.contains("2"));
    assertFalse(hugeSet.contains("test"));
  }

  @Test
  void containsAll() {
    List<String> contents = Arrays.asList("test1", "test2", "test3");
    hugeSet.addAll(contents);
    assertTrue(hugeSet.containsAll(contents));
  }

  @Test
  void containsAllLarge() {
    fillToThreshold();
    Set<String> contents = new HashSet<>();
    IntStream.range(0, HugeSet.THRESHOLD).mapToObj(Integer::toString).forEach(contents::add);
    assertTrue(hugeSet.containsAll(contents));
  }

  @Test
  void iterator() {
    List<String> contents = Arrays.asList("test1", "test2", "test3");
    hugeSet.addAll(contents);

    Iterator<String> it = hugeSet.iterator();
    assertEquals(Iterators.size(it), contents.size());

    for (String s : hugeSet) {
      assertTrue(contents.contains(s));
    }
  }

  @Test
  void iteratorLarge() {
    fillToThreshold();

    Set<String> contents = new HashSet<>();
    IntStream.range(0, HugeSet.THRESHOLD).mapToObj(Integer::toString).forEach(contents::add);

    Iterator<String> it = hugeSet.iterator();
    assertEquals(Iterators.size(it), contents.size());

    for (String s : hugeSet) {
      assertTrue(contents.contains(s));
    }
  }

  @Test
  void remove() {
    List<String> contents = Arrays.asList("test1", "test2", "test3");
    hugeSet.addAll(contents);
    hugeSet.remove("test1");
    assertEquals(hugeSet.size(), 2);
  }

  @Test
  void removeLarge() {
    fillToThreshold();
    hugeSet.remove("1");
    assertEquals(hugeSet.size(), HugeSet.THRESHOLD - 1);
  }

  @Test
  void removeAll() {
    List<String> contents = Arrays.asList("test1", "test2", "test3");
    hugeSet.addAll(contents);
    hugeSet.removeAll(contents);
    assertEquals(hugeSet.size(), 0);
  }

  @Test
  void removeAllLarge() {
    fillToThreshold();

    Set<String> contents = new HashSet<>();
    IntStream.range(0, HugeSet.THRESHOLD).mapToObj(Integer::toString).forEach(contents::add);

    hugeSet.removeAll(contents);
    assertEquals(hugeSet.size(), 0);
  }

  @Test
  void retainAll() {
    List<String> contents = Arrays.asList("test1", "test2", "test3");
    hugeSet.addAll(contents);
    hugeSet.retainAll(Arrays.asList("test2", "test3"));
    assertEquals(hugeSet.size(), 2);
  }

  @Test
  void retainAllLarge() {
    fillToThreshold();
    hugeSet.retainAll(Arrays.asList("2", "3"));
    assertEquals(hugeSet.size(), 2);
  }

  @Test
  void toArray() {
    List<String> contents = Arrays.asList("test1", "test2", "test3");
    hugeSet.addAll(contents);
    assertEquals(hugeSet.toArray().length, 3);
  }

  @Test
  void toArrayLarge() {
    fillToThreshold();
    assertEquals(hugeSet.toArray().length, HugeSet.THRESHOLD);
  }

  private void fillToThreshold() {
    IntStream.range(0, HugeSet.THRESHOLD).mapToObj(Integer::toString).forEach(hugeSet::add);
  }
}
