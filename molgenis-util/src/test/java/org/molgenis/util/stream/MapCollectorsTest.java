package org.molgenis.util.stream;

import static java.util.function.Function.identity;
import static org.testng.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.testng.annotations.Test;

public class MapCollectorsTest {
  @Test
  public void testToLinkedMap() {
    Map<String, Integer> expectedLinkedMap = new LinkedHashMap<>();
    expectedLinkedMap.put("a", 1);
    expectedLinkedMap.put("bb", 2);
    expectedLinkedMap.put("ccc", 3);
    Map<String, Integer> actualLinkedMap =
        Stream.of("a", "bb", "ccc").collect(MapCollectors.toLinkedMap(identity(), String::length));
    assertEquals(expectedLinkedMap, actualLinkedMap);
  }

  @Test(
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = "Duplicate key detected with values '1' and '2'")
  public void testToLinkedMapDuplicateKey() {
    //noinspection ResultOfMethodCallIgnored
    Stream.of("a1", "a2")
        .collect(MapCollectors.toLinkedMap(str -> str.charAt(0), str -> str.charAt(1)));
  }
}
