package org.molgenis.data.rest;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.mockito.Mockito;
import org.molgenis.data.EntityMetaData;
import org.molgenis.security.core.MolgenisPermissionService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityCollectionResponseTest
{
	private EntityMetaData entityMetaData;
	private MolgenisPermissionService permissionService;

	@BeforeMethod
	public void setUp()
	{
		entityMetaData = Mockito.mock(EntityMetaData.class);
		permissionService = mock(MolgenisPermissionService.class);
	}

	@Test
	public void getNextHref()
	{
		EntityPager pager = new EntityPager(0, 10, 25l, null);
		EntityCollectionResponse response = new EntityCollectionResponse(pager, null, "/test", entityMetaData,
				permissionService);
		assertEquals(response.getNextHref(), "/test?start=10&num=10");

		pager = new EntityPager(10, 10, 25l, null);
		response = new EntityCollectionResponse(pager, null, "/test", entityMetaData, permissionService);
		assertEquals(response.getNextHref(), "/test?start=20&num=10");

		pager = new EntityPager(0, 25, 25l, null);
		response = new EntityCollectionResponse(pager, null, "/test", entityMetaData, permissionService);
		assertNull(response.getNextHref());
	}

	@Test
	public void getPrevHref()
	{
		EntityPager pager = new EntityPager(0, 15, 30l, null);
		EntityCollectionResponse response = new EntityCollectionResponse(pager, null, "/test", entityMetaData,
				permissionService);
		assertNull(response.getPrevHref());

		pager = new EntityPager(15, 15, 30l, null);
		response = new EntityCollectionResponse(pager, null, "/test", entityMetaData, permissionService);
		assertEquals(response.getPrevHref(), "/test?start=0&num=15");

		pager = new EntityPager(30, 15, 30l, null);
		response = new EntityCollectionResponse(pager, null, "/test", entityMetaData, permissionService);
		assertEquals(response.getPrevHref(), "/test?start=15&num=15");
	}
}
