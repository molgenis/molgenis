package org.molgenis.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterators;

public class HugeSetTest
{
	private HugeSet<String> hugeSet;

	@BeforeMethod
	public void beforeMethod()
	{
		hugeSet = new HugeSet<String>();
	}

	@AfterMethod
	public void afterMethod() throws IOException
	{
		hugeSet.close();
	}

	@Test
	public void add()
	{
		hugeSet.add("test");
	}

	@Test
	public void clear()
	{
		List<String> contents = Arrays.asList("test1", "test2", "test3");
		hugeSet.addAll(contents);
		hugeSet.clear();
		assertTrue(hugeSet.isEmpty());
	}

	@Test
	public void contains()
	{
		List<String> contents = Arrays.asList("test1", "test2", "test3");
		hugeSet.addAll(contents);
		assertTrue(hugeSet.contains("test2"));
		assertFalse(hugeSet.contains("test4"));
	}

	@Test
	public void containsAll()
	{
		List<String> contents = Arrays.asList("test1", "test2", "test3");
		hugeSet.addAll(contents);
		assertTrue(hugeSet.containsAll(contents));
	}

	@Test
	public void iterator()
	{
		List<String> contents = Arrays.asList("test1", "test2", "test3");
		hugeSet.addAll(contents);

		Iterator<String> it = hugeSet.iterator();
		assertEquals(Iterators.size(it), contents.size());

		for (String s : hugeSet)
		{
			assertTrue(contents.contains(s));
		}
	}

	@Test
	public void remove()
	{
		List<String> contents = Arrays.asList("test1", "test2", "test3");
		hugeSet.addAll(contents);
		hugeSet.remove("test1");
		assertEquals(hugeSet.size(), 2);
	}

	@Test
	public void removeAll()
	{
		List<String> contents = Arrays.asList("test1", "test2", "test3");
		hugeSet.addAll(contents);
		hugeSet.removeAll(contents);
		assertEquals(hugeSet.size(), 0);
	}

	@Test
	public void retainAll()
	{
		List<String> contents = Arrays.asList("test1", "test2", "test3");
		hugeSet.addAll(contents);
		hugeSet.retainAll(Arrays.asList("test2", "test3"));
		assertEquals(hugeSet.size(), 2);
	}

	@Test
	public void size()
	{
		hugeSet.add("test1");
		hugeSet.add("test2");
		hugeSet.add("test3");

		assertEquals(hugeSet.size(), 3);
	}

	@Test
	public void toArray()
	{
	}

	@Test
	public void toArrayT()
	{
	}
}
