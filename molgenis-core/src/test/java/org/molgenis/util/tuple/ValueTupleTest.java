package org.molgenis.util.tuple;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ValueTupleTest
{
	private ValueTuple tuple;

	@BeforeMethod
	public void setUp()
	{
		tuple = new ValueTuple(Arrays.asList("val1", "val2", "val3"));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ValueTuple()
	{
		new ValueTuple(null);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void getString()
	{
		tuple.get("col1");
	}

	@Test
	public void getint()
	{
		assertEquals(tuple.get(0), "val1");
		assertEquals(tuple.get(1), "val2");
		assertEquals(tuple.get(2), "val3");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void getColNames()
	{
		tuple.getColNames();
	}

	@Test
	public void getNrCols()
	{
		assertEquals(tuple.getNrCols(), 3);
	}
}
