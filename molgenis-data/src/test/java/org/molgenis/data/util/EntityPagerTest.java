package org.molgenis.data.util;

import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class EntityPagerTest
{
	@Test
	public void getNextStart()
	{
		EntityPager<DynamicEntity> entityPager = new EntityPager<>(2, 5, 10, null);
		assertEquals(entityPager.getNextStart(), Integer.valueOf(7));
	}

	@Test
	public void getNextStart_limit()
	{
		EntityPager<DynamicEntity> entityPager = new EntityPager<>(2, 5, 4, null);
		assertNull(entityPager.getNextStart());
	}

	@Test
	public void getNextStart_borderLeft()
	{
		EntityPager<DynamicEntity> entityPager = new EntityPager<>(0, 3, 10, null);
		assertEquals(entityPager.getNextStart(), Integer.valueOf(3));
	}

	@Test
	public void getNextStart_borderRight()
	{
		EntityPager<DynamicEntity> entityPager = new EntityPager<>(0, 1, 2, null);
		assertEquals(entityPager.getNextStart(), Integer.valueOf(1));
	}

	@Test
	public void getPrevStart()
	{
		EntityPager<DynamicEntity> entityPager = new EntityPager<>(8, 5, 10, null);
		assertEquals(entityPager.getPrevStart(), Integer.valueOf(3));
	}

	@Test
	public void getPrevStart_offset()
	{
		EntityPager<DynamicEntity> entityPager = new EntityPager<>(0, 3, 10, null);
		assertNull(entityPager.getPrevStart());
	}

	@Test
	public void getPrevStart_borderLeft()
	{
		EntityPager<DynamicEntity> entityPager = new EntityPager<>(3, 3, 10, null);
		assertEquals(entityPager.getPrevStart(), Integer.valueOf(0));
	}

}
