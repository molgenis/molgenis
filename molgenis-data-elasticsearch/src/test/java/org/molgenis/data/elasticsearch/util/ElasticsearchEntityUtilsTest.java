package org.molgenis.data.elasticsearch.util;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
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
				Arrays.<String>asList(id0, id1));
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
				Lists.newArrayList(ElasticsearchEntityUtils.toEntityIds(Arrays.<String>asList(id0, id1))));
	}

	@Test
	public void toElasticsearchIdEntityEntityMetaData()
	{
		String id = "id0";
		String idAttributeName = "id";
		Entity entity = mock(Entity.class);
		when(entity.get(idAttributeName)).thenReturn(id);
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		AttributeMetaData idAttribute = mock(AttributeMetaData.class);
		when(idAttribute.getName()).thenReturn(idAttributeName);
		when(entityMetaData.getIdAttribute()).thenReturn(idAttribute);
		assertEquals(id, ElasticsearchEntityUtils.toElasticsearchId(entity, entityMetaData));
	}
}
