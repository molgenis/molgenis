package org.molgenis.data.elasticsearch;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

public class ElasticsearchEntityFactoryTest
{
	@Test(expectedExceptions = NullPointerException.class)
	public void ElasticsearchEntityFactory()
	{
		new ElasticsearchEntityFactory(null);
	}

	@Test
	public void create()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Entity entity = mock(Entity.class);
	}
}
