package org.molgenis.data.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;

class BatchingQueryResultTest {
  @Test
  void getBatch() {
    final int batchSize = 2;
    BatchingQueryResult<Entity> bqr = new DummyBatchingQueryResult(batchSize);
    assertEquals(Iterables.size(bqr), 4);
  }

  private static class DummyBatchingQueryResult extends BatchingQueryResult<Entity> {
    private final int batchSize;
    int batchCount;

    private DummyBatchingQueryResult(int batchSize) {
      super(batchSize, new QueryImpl<>());
      this.batchSize = batchSize;
      batchCount = 0;
    }

    @Override
    protected List<Entity> getBatch(Query<Entity> q) {
      assertEquals(q.getOffset(), batchCount * batchSize);
      assertEquals(q.getPageSize(), batchSize);

      if (++batchCount == 3) return Lists.newArrayList();
      return Arrays.asList(
          new DynamicEntity(mock(EntityType.class)), new DynamicEntity(mock(EntityType.class)));
    }
  }
}
