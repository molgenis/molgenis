package org.molgenis.data.rest.v2;

import org.molgenis.data.Entity;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.v2.EntityAggregatesResponse.AggregateResultResponse;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.testng.Assert.assertEquals;

public class EntityAggregatesResponseTest extends AbstractMockitoTest
{
	// regression test for https://github.com/molgenis/molgenis/issues/6581
	@Test
	public void testAggregateResultResponseToResponseAggregateResult()
	{
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

		AggregateResultResponse aggregateResultResponse = AggregateResultResponse.toResponse(aggregateResult);
		assertEquals(aggregateResultResponse.getMatrix(), matrix);
		assertEquals(aggregateResultResponse.getxLabels(), singletonList(singletonMap("longAttribute", "123")));
		assertEquals(aggregateResultResponse.getyLabels(), emptyList());
	}
}