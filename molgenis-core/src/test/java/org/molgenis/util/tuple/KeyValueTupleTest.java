package org.molgenis.util.tuple;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class KeyValueTupleTest
{
	private KeyValueTuple tuple;

	@BeforeMethod
	public void setUp()
	{
		tuple = new KeyValueTuple();
		tuple.set("col1", "val1");
		tuple.set("col2", "val2");
		tuple.set("col3", "val3");
	}

	@Test
	public void getString()
	{
		assertEquals(tuple.get("col1"), "val1");
		assertEquals(tuple.get("col2"), "val2");
		assertEquals(tuple.get("col3"), "val3");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void getint()
	{
		assertEquals(tuple.get(0), "val1");
	}

	@Test
	public void getColNames()
	{
		Iterator<String> it = tuple.getColNames().iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next(), "col1");
		assertTrue(it.hasNext());
		assertEquals(it.next(), "col2");
		assertTrue(it.hasNext());
		assertEquals(it.next(), "col3");
		assertFalse(it.hasNext());
	}
}
