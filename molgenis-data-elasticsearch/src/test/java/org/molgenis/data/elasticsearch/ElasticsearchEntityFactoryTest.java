package org.molgenis.data.elasticsearch;

import org.testng.annotations.Test;

public class ElasticsearchEntityFactoryTest
{
	@Test(expectedExceptions = NullPointerException.class)
	public void ElasticsearchEntityFactory()
	{
		new ElasticsearchEntityFactory(null, null);
	}
}
