package org.molgenis.util.stream;

import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class MapCollectorsTest {
  @Test
  void testToLinkedMap() {
    Map<String, Integer> expectedLinkedMap = new LinkedHashMap<>();
    expectedLinkedMap.put("a", 1);
    expectedLinkedMap.put("bb", 2);
    expectedLinkedMap.put("ccc", 3);
    Map<String, Integer> actualLinkedMap =
        Stream.of("a", "bb", "ccc").collect(MapCollectors.toLinkedMap(identity(), String::length));
    assertEquals(actualLinkedMap, expectedLinkedMap);
  }

  @Test
  void testToLinkedMapDuplicateKey() {
    //noinspection ResultOfMethodCallIgnored
    Exception exception =
        assertThrows(
            IllegalStateException.class,
            () ->
                Stream.of("a1", "a2")
                    .collect(
                        MapCollectors.toLinkedMap(str -> str.charAt(0), str -> str.charAt(1))));
    assertEquals(exception.getMessage(), "Duplicate key detected with values '1' and '2'");
  }
}
