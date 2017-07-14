package org.molgenis.util;

import com.google.common.collect.Iterators;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import static org.testng.Assert.*;

public class HugeSetTest
{
	private HugeSet<String> hugeSet;

	@BeforeMethod
	public void beforeMethod()
	{
		hugeSet = new HugeSet<>();
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
		assertEquals(hugeSet.size(), 1);
	}

	@Test
	public void addLarge()
	{
		fillToThreshold();
		assertEquals(hugeSet.size(), HugeSet.THRESHOLD);
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
	public void clearLarge()
	{
		fillToThreshold();
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
	public void containsLarge()
	{
		fillToThreshold();
		assertTrue(hugeSet.contains("2"));
		assertFalse(hugeSet.contains("test"));
	}

	@Test
	public void containsAll()
	{
		List<String> contents = Arrays.asList("test1", "test2", "test3");
		hugeSet.addAll(contents);
		assertTrue(hugeSet.containsAll(contents));
	}

	@Test
	public void containsAllLarge()
	{
		fillToThreshold();
		Set<String> contents = new HashSet<>();
		IntStream.range(0, HugeSet.THRESHOLD).mapToObj(Integer::toString).forEach(contents::add);
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
	public void iteratorLarge()
	{
		fillToThreshold();

		Set<String> contents = new HashSet<>();
		IntStream.range(0, HugeSet.THRESHOLD).mapToObj(Integer::toString).forEach(contents::add);

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
	public void removeLarge()
	{
		fillToThreshold();
		hugeSet.remove("1");
		assertEquals(hugeSet.size(), HugeSet.THRESHOLD - 1);
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
	public void removeAllLarge()
	{
		fillToThreshold();

		Set<String> contents = new HashSet<>();
		IntStream.range(0, HugeSet.THRESHOLD).mapToObj(Integer::toString).forEach(contents::add);

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
	public void retainAllLarge()
	{
		fillToThreshold();
		hugeSet.retainAll(Arrays.asList("2", "3"));
		assertEquals(hugeSet.size(), 2);
	}

	@Test
	public void toArray()
	{
		List<String> contents = Arrays.asList("test1", "test2", "test3");
		hugeSet.addAll(contents);
		assertEquals(hugeSet.toArray().length, 3);
	}

	@Test
	public void toArrayLarge()
	{
		fillToThreshold();
		assertEquals(hugeSet.toArray().length, HugeSet.THRESHOLD);
	}

	private void fillToThreshold()
	{
		IntStream.range(0, HugeSet.THRESHOLD).mapToObj(Integer::toString).forEach(hugeSet::add);
	}
}
