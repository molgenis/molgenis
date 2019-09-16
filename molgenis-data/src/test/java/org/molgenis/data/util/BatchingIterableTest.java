package org.molgenis.data.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BatchingIterableTest {
  private static List<Integer> ITEMS_LIST;

  @BeforeAll
  static void setUpBeforeClass() {
    ITEMS_LIST = Arrays.asList(1, 2, 3, 4);
  }

  // parameterized test testing combinations of offset, limit, batchSize
  static Iterator<Object[]> createData1() {
    List<Object[]> paramList = new ArrayList<>();

    for (int batchSize = 1; batchSize < ITEMS_LIST.size() + 1; ++batchSize) {
      for (int offset = 0; offset < ITEMS_LIST.size(); ++offset) {
        // add limit that is larger than the number of items
        for (int limit = 1; limit < ITEMS_LIST.size() - offset + 1; ++limit) {
          paramList.add(new Object[] {offset, limit, batchSize});
        }
        // add limit=0 (no limit) add the end to enable friendly printing order
        paramList.add(new Object[] {offset, 0, batchSize});
      }
    }

    return paramList.iterator();
  }

  @ParameterizedTest
  @MethodSource("createData1")
  void iterator(int offset, int limit, int batchSize) {
    Iterable<Integer> iterable =
        new BatchingIterable<>(batchSize, offset, limit) {
          @Override
          protected List<Integer> getBatch(int offset, int batchSize) {
            List<Integer> batchList;
            if (offset < ITEMS_LIST.size()) {
              int toIndex = offset + Math.min(batchSize, ITEMS_LIST.size() - offset);
              batchList = ITEMS_LIST.subList(offset, toIndex);
            } else {
              batchList = Collections.emptyList();
            }
            return batchList;
          }
        };

    int expectedValue = offset + 1;
    int actualNrItems = 0;
    for (Integer anIterable : iterable) {
      int intValue = anIterable;
      assertEquals(expectedValue++, intValue);
      ++actualNrItems;
    }

    // enable to print batch info
    // System.out.println(String.format("[offset=%d, limit=%d, batchSize=%d]", offset, limit,
    // batchSize) + ": "
    // + StringUtils.join(iterable, ','));
    int expectedNrItems = limit == 0 ? ITEMS_LIST.size() - offset : limit;
    assertEquals(
        actualNrItems,
        expectedNrItems,
        String.format("[offset=%d, limit=%d, batchSize=%d]", offset, limit, batchSize));
  }

  @Test
  void iteratorNoResults() {
    Iterable<Integer> iterable =
        new BatchingIterable<>(1000, 0, 10) {
          @Override
          protected List<Integer> getBatch(int offset, int batchSize) {
            return Collections.emptyList();
          }
        };
    assertFalse(iterable.iterator().hasNext());
  }
}
