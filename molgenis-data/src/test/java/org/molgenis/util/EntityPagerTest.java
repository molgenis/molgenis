package org.molgenis.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

public class EntityPagerTest
{
	@Test
	public void getNextStart()
	{
		EntityPager<MapEntity> entityPager = new EntityPager<MapEntity>(2, 5, 10, null);
		assertEquals(entityPager.getNextStart(), Integer.valueOf(7));
	}

	@Test
	public void getNextStart_limit()
	{
		EntityPager<MapEntity> entityPager = new EntityPager<MapEntity>(2, 5, 4, null);
		assertNull(entityPager.getNextStart());
	}

	@Test
	public void getNextStart_borderLeft()
	{
		EntityPager<MapEntity> entityPager = new EntityPager<MapEntity>(0, 3, 10, null);
		assertEquals(entityPager.getNextStart(), Integer.valueOf(3));
	}

	@Test
	public void getNextStart_borderRight()
	{
		EntityPager<MapEntity> entityPager = new EntityPager<MapEntity>(0, 1, 2, null);
		assertEquals(entityPager.getNextStart(), Integer.valueOf(1));
	}

	@Test
	public void getPrevStart()
	{
		EntityPager<MapEntity> entityPager = new EntityPager<MapEntity>(8, 5, 10, null);
		assertEquals(entityPager.getPrevStart(), Integer.valueOf(3));
	}

	@Test
	public void getPrevStart_offset()
	{
		EntityPager<MapEntity> entityPager = new EntityPager<MapEntity>(0, 3, 10, null);
		assertNull(entityPager.getPrevStart());
	}

	@Test
	public void getPrevStart_borderLeft()
	{
		EntityPager<MapEntity> entityPager = new EntityPager<MapEntity>(3, 3, 10, null);
		assertEquals(entityPager.getPrevStart(), Integer.valueOf(0));
	}

}
