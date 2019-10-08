package org.molgenis.api.data.v2;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.LONG;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.molgenis.api.data.v2.EntityAggregatesResponse.AggregateResultResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class EntityAggregatesResponseTest extends AbstractMockitoTest {
  // regression test for https://github.com/molgenis/molgenis/issues/6581
  @Test
  void testAggregateResultResponseToResponseAggregateResult() {
    EntityType entityType = mock(EntityType.class);
    Attribute longAttribute = when(mock(Attribute.class).getDataType()).thenReturn(LONG).getMock();
    when(longAttribute.getName()).thenReturn("longAttribute");
    Iterable<Attribute> attributes = singletonList(longAttribute);
    when(entityType.getAtomicAttributes()).thenReturn(attributes);

    Entity entity = mock(Entity.class);
    when(entity.get("longAttribute")).thenReturn(123L);
    when(entity.getEntityType()).thenReturn(entityType);

    List<List<Long>> matrix = emptyList();
    List<Object> xLabels = Collections.singletonList(entity);
    List<Object> yLabels = emptyList();

    AggregateResult aggregateResult = mock(AggregateResult.class);
    when(aggregateResult.getMatrix()).thenReturn(matrix);
    when(aggregateResult.getxLabels()).thenReturn(xLabels);
    when(aggregateResult.getyLabels()).thenReturn(yLabels);

    AggregateResultResponse aggregateResultResponse =
        AggregateResultResponse.toResponse(aggregateResult);
    assertEquals(matrix, aggregateResultResponse.getMatrix());
    assertEquals(
        singletonList(singletonMap("longAttribute", "123")), aggregateResultResponse.getxLabels());
    assertEquals(emptyList(), aggregateResultResponse.getyLabels());
  }
}
