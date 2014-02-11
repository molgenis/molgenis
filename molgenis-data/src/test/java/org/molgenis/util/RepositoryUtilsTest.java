package org.molgenis.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.data.Countable;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

public class RepositoryUtilsTest
{
	@Test
	public void count()
	{
		Countable countable = mock(Countable.class, withSettings().extraInterfaces(Repository.class));
		when(countable.count()).thenReturn(100l);
		long count = RepositoryUtils.count((Repository) countable);
		assertEquals(count, 100);

		Repository repo = mock(Repository.class);
		when(repo.iterator()).thenReturn(Arrays.<Entity> asList(new MapEntity()).iterator());
		count = RepositoryUtils.count(repo);
		assertEquals(count, 1);
	}
}
