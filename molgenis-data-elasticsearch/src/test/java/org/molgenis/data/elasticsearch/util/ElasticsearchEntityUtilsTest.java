package org.molgenis.data.elasticsearch.util;

import com.google.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ElasticsearchEntityUtilsTest
{

	@Test
	public void toElasticsearchIdObject()
	{
		String id = "id";
		assertEquals(id, ElasticsearchEntityUtils.toElasticsearchId(id));
	}

	@Test
	public void toElasticsearchIds()
	{
		String id0 = "id0";
		String id1 = "id1";
		assertEquals(ElasticsearchEntityUtils.toElasticsearchIds(Stream.of(id0, id1)).collect(toList()),
				Arrays.asList(id0, id1));
	}

	@Test
	public void toEntityId()
	{
		String id = "id0";
		assertEquals(id, ElasticsearchEntityUtils.toEntityId(id));
	}

	@Test
	public void toEntityIds()
	{
		String id0 = "id0";
		String id1 = "id1";
		assertEquals(Arrays.<Object>asList(id0, id1),
				Lists.newArrayList(ElasticsearchEntityUtils.toEntityIds(Arrays.asList(id0, id1))));
	}

	@Test
	public void toElasticsearchIdEntityEntityType()
	{
		String id = "id0";
		String idAttributeName = "id";
		Entity entity = mock(Entity.class);
		when(entity.get(idAttributeName)).thenReturn(id);
		EntityType entityType = mock(EntityType.class);
		Attribute idAttribute = mock(Attribute.class);
		when(idAttribute.getName()).thenReturn(idAttributeName);
		when(entityType.getIdAttribute()).thenReturn(idAttribute);
		assertEquals(id, ElasticsearchEntityUtils.toElasticsearchId(entity, entityType));
	}
}
