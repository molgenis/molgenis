package org.molgenis.data.rest;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class EntityPagerTest
{

	@Test
	public void getNextStart()
	{
		EntityPager pager = new EntityPager(10, 15, null, null);
		assertEquals(pager.getNextStart(), Integer.valueOf(25));

		pager = new EntityPager(0, 10, 25L, null);
		assertEquals(pager.getNextStart(), Integer.valueOf(10));

		pager = new EntityPager(10, 10, 25L, null);
		assertEquals(pager.getNextStart(), Integer.valueOf(20));

		pager = new EntityPager(0, 25, 25L, null);
		assertNull(pager.getNextStart());
	}

	@Test
	public void getPrevStart()
	{
		EntityPager pager = new EntityPager(10, 15, null, null);
		assertEquals(pager.getPrevStart(), Integer.valueOf(0));

		pager = new EntityPager(0, 15, 30L, null);
		assertNull(pager.getPrevStart());

		pager = new EntityPager(15, 15, 30L, null);
		assertEquals(pager.getPrevStart(), Integer.valueOf(0));

		pager = new EntityPager(30, 15, 30L, null);
		assertEquals(pager.getPrevStart(), Integer.valueOf(15));
	}
}
