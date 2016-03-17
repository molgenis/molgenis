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
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

public class MetaDataRepositoryDecoratorTest
{
	private Repository repo;
	private Repository decorator;

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
	public void streamFetch()
	{
		Fetch fetch = new Fetch();
		decorator.stream(fetch);
		verify(repo, times(1)).stream(fetch);
	}
}
