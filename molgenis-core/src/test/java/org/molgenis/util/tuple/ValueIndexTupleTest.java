package org.molgenis.util.tuple;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ValueIndexTupleTest
{
	private ValueIndexTuple indexedTuple;

	@BeforeMethod
	public void setUp()
	{
		List<String> values = Arrays.asList("val1", "val2", "val3");
		Map<String, Integer> colNamesIndex = new LinkedHashMap<String, Integer>();
		colNamesIndex.put("col1", 0);
		colNamesIndex.put("col2", 1);
		colNamesIndex.put("col3", 2);
		indexedTuple = new ValueIndexTuple(colNamesIndex, values);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ValueIndexTuple()
	{
		new ValueIndexTuple(null, null);
	}

	@Test
	public void getString()
	{
		assertEquals(indexedTuple.get("col1"), "val1");
		assertEquals(indexedTuple.get("col2"), "val2");
		assertEquals(indexedTuple.get("col3"), "val3");
	}

	@Test
	public void getint()
	{
		assertEquals(indexedTuple.get(0), "val1");
		assertEquals(indexedTuple.get(1), "val2");
		assertEquals(indexedTuple.get(2), "val3");
	}

	@Test
	public void getColNames()
	{
		Iterator<String> it = indexedTuple.getColNames().iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next(), "col1");
		assertTrue(it.hasNext());
		assertEquals(it.next(), "col2");
		assertTrue(it.hasNext());
		assertEquals(it.next(), "col3");
		assertFalse(it.hasNext());
	}

	@Test
	public void getNrCols()
	{
		assertEquals(indexedTuple.getNrCols(), 3);
	}
}
