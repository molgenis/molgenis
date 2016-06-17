package org.molgenis.data.meta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.RepositoryCapability.INDEXABLE;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.testng.Assert.assertEquals;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

import java.util.List;
import java.util.function.Consumer;

public class MetaDataRepositoryDecoratorTest
{
	private Repository<Entity> repo;
	private Repository<Entity> decorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		repo = mock(Repository.class);
		decorator = new MetaDataRepositoryDecorator(repo);
	}

	@Test
	public void getCapabilities()
	{
		when(repo.getCapabilities()).thenReturn(Sets.newHashSet(INDEXABLE, QUERYABLE, WRITABLE, MANAGABLE));
		assertEquals(decorator.getCapabilities(), Sets.newHashSet(INDEXABLE, QUERYABLE));
		IOUtils.closeQuietly(decorator);
	}

	@Test
	public void forEachBatchedFetch()
	{
		Fetch fetch = new Fetch();
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		repo.forEachBatched(fetch, consumer, 234);
		verify(repo, times(1)).forEachBatched(fetch, consumer, 234);
	}
}
