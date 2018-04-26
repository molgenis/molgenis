package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertFalse;

public class DefaultEntityCollectionTest
{
	@SuppressWarnings("unchecked")
	@Test
	public void isLazy()
	{
		Iterable<Entity> entities = mock(Iterable.class);
		Iterable<String> attrNames = mock(Iterable.class);
		assertFalse(new DefaultEntityCollection(entities, attrNames).isLazy());
	}
}
