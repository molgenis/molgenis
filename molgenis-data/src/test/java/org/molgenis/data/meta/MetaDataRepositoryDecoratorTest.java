package org.molgenis.data.meta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.RepositoryCapability.INDEXABLE;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.testng.Assert.assertEquals;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.Repository;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

public class MetaDataRepositoryDecoratorTest
{

	@Test
	public void getCapabilities()
	{
		Repository repo = mock(Repository.class);
		when(repo.getCapabilities()).thenReturn(Sets.newHashSet(INDEXABLE, QUERYABLE, WRITABLE, MANAGABLE));
		Repository decorator = new MetaDataRepositoryDecorator(repo);
		assertEquals(decorator.getCapabilities(), Sets.newHashSet(INDEXABLE, QUERYABLE));
		IOUtils.closeQuietly(decorator);
	}
}
