package org.molgenis.util.tuple;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CaseInsensitiveKeyValueTupleTest
{
	private CaseInsensitiveKeyValueTuple tuple;

	@BeforeMethod
	public void setUp()
	{
		tuple = new CaseInsensitiveKeyValueTuple();
		tuple.set("col1", "val1");
		tuple.set("COL2", "val2");
		tuple.set("col3", "VAL3");
		tuple.set("COL4", "VAL4");
	}

	@Test
	public void get()
	{
		assertEquals("val1", tuple.get("col1"));
		assertEquals("val1", tuple.get("COL1"));
		assertEquals("val2", tuple.get("col2"));
		assertEquals("val2", tuple.get("COL2"));
		assertEquals("VAL3", tuple.get("col3"));
		assertEquals("VAL3", tuple.get("COL3"));
		assertEquals("VAL4", tuple.get("col4"));
		assertEquals("VAL4", tuple.get("COL4"));
	}

	@Test
	public void set()
	{
		tuple.set("COL5", "val5");
		assertEquals("val5", tuple.get("col5"));
		assertEquals("val5", tuple.get("COL5"));
	}
}
