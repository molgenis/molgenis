package org.molgenis.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

public class EntityPagerTest
{
	@Test
	public void getNextStart()
	{
		EntityPager<Entity> entityPager = new EntityPager<Entity>(2, 5, 10, null);
		assertEquals(entityPager.getNextStart(), Integer.valueOf(7));
	}

	@Test
	public void getNextStart_limit()
	{
		EntityPager<Entity> entityPager = new EntityPager<Entity>(2, 5, 4, null);
		assertNull(entityPager.getNextStart());
	}

	@Test
	public void getNextStart_borderLeft()
	{
		EntityPager<Entity> entityPager = new EntityPager<Entity>(0, 3, 10, null);
		assertEquals(entityPager.getNextStart(), Integer.valueOf(3));
	}

	@Test
	public void getNextStart_borderRight()
	{
		EntityPager<Entity> entityPager = new EntityPager<Entity>(0, 1, 2, null);
		assertEquals(entityPager.getNextStart(), Integer.valueOf(1));
	}

	@Test
	public void getPrevStart()
	{
		EntityPager<Entity> entityPager = new EntityPager<Entity>(8, 5, 10, null);
		assertEquals(entityPager.getPrevStart(), Integer.valueOf(3));
	}

	@Test
	public void getPrevStart_offset()
	{
		EntityPager<Entity> entityPager = new EntityPager<Entity>(0, 3, 10, null);
		assertNull(entityPager.getPrevStart());
	}

	@Test
	public void getPrevStart_borderLeft()
	{
		EntityPager<Entity> entityPager = new EntityPager<Entity>(3, 3, 10, null);
		assertEquals(entityPager.getPrevStart(), Integer.valueOf(0));
	}

}
