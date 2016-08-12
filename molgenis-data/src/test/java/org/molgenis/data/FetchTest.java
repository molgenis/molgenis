package org.molgenis.data;

import com.google.common.collect.Sets;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.Map.Entry;

import static org.testng.Assert.*;

public class FetchTest
{
	@Test
	public void equalsTrue()
	{
		String field = "field";
		assertEquals(new Fetch().field(field), new Fetch().field(field));
	}

	@Test
	public void equalsFalse()
	{
		assertFalse(new Fetch().field("field0").equals(new Fetch().field("field1")));
	}

	@Test
	public void equalsSubFetchTrue()
	{
		String field = "field";
		Fetch subFetch = new Fetch();
		assertTrue(new Fetch().field(field, subFetch).equals(new Fetch().field(field, subFetch)));
	}

	@Test
	public void equalsSubFetchFalse()
	{
		String field = "field";
		Fetch subFetch = new Fetch();
		assertFalse(new Fetch().field(field, subFetch).equals(new Fetch().field(field)));
	}

	@Test
	public void getFetch()
	{
		String field = "field";
		Fetch subFetch = new Fetch();
		assertEquals(subFetch, new Fetch().field(field, subFetch).getFetch(field));
	}

	@Test
	public void getFields()
	{
		String field0 = "field0";
		String field1 = "field1";
		String field2 = "field2";
		Fetch fetch = new Fetch().field(field0).field(field1).field(field2);

		assertEquals(Sets.newHashSet(field0, field1, field2), fetch.getFields());
	}

	@Test
	public void hasFieldTrue()
	{
		String field = "field";
		assertTrue(new Fetch().field(field).hasField(field));
	}

	@Test
	public void hasFieldFalse()
	{
		String field = "field";
		assertFalse(new Fetch().hasField(field));
	}

	@Test
	public void iterator()
	{
		String field0 = "field0";
		String field1 = "field1";
		String field2 = "field2";
		Fetch fetch = new Fetch().field(field0).field(field1).field(field2);

		Iterator<Entry<String, Fetch>> it = fetch.iterator();
		assertTrue(it.hasNext());
		assertEquals("field0", it.next().getKey());
		assertTrue(it.hasNext());
		assertEquals("field1", it.next().getKey());
		assertTrue(it.hasNext());
		assertEquals("field2", it.next().getKey());
		assertFalse(it.hasNext());
	}
}
