package org.molgenis.util.tuple;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.testng.annotations.Test;

public class SingletonTupleTest
{
	@Test
	public void getString()
	{
		assertEquals(new SingletonTuple<Integer>("col1", 1).get("col1"), 1);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void getint()
	{
		new SingletonTuple<Integer>("col1", 1).get(0);
	}

	@Test
	public void getColNames()
	{
		Iterator<String> it = new SingletonTuple<Integer>("col1", 1).getColNames().iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next(), "col1");
		assertFalse(it.hasNext());
	}

	@Test
	public void getNrCols()
	{
		assertEquals(new SingletonTuple<Integer>("col1", 1).getNrCols(), 1);
	}
}
