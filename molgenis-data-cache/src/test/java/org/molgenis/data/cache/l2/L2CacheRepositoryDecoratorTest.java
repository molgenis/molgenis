package org.molgenis.data.cache.l2;

import autovalue.shaded.com.google.common.common.collect.Sets;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class L2CacheRepositoryDecoratorTest
{
	private L2CacheRepositoryDecorator cacheableDecorator;
	@Mock
	private L2Cache l2Cache;
	@Mock
	private Repository<Entity> cacheableRepository;
	@Mock
	private Entity entity;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
		when(cacheableRepository.getCapabilities()).thenReturn(Sets.newHashSet(CACHEABLE, WRITABLE));
		cacheableDecorator = new L2CacheRepositoryDecorator(cacheableRepository, l2Cache);
	}

	@Test
	public void testFindOneByIdCacheableAndPresent()
	{
		when(l2Cache.get(cacheableRepository, "abcde")).thenReturn(entity);
		assertEquals(cacheableDecorator.findOneById("abcde"), entity);
	}

	@Test
	public void testFindOneByIdCacheableNotPresent()
	{
		when(l2Cache.get(cacheableRepository, "abcde")).thenReturn(null);
		assertNull(cacheableDecorator.findOneById("abcde"));
	}
}
