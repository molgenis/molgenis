package org.molgenis.util;

import com.google.common.collect.Sets;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.testng.Assert.*;

public class HugeMapTest
{
	private HugeMap<String, String> hugeMap;

	@BeforeMethod
	public void beforeMethod()
	{
		hugeMap = new HugeMap<>();
	}

	@AfterMethod
	public void afterMethod() throws IOException
	{
		hugeMap.close();
	}

	@Test
	public void clear()
	{
		hugeMap.put("key", "value");
		hugeMap.clear();
		assertTrue(hugeMap.isEmpty());
	}

	@Test
	public void clearLarge()
	{
		fillToThreshold();
		hugeMap.clear();
		assertTrue(hugeMap.isEmpty());
	}

	@Test
	public void containsKey()
	{
		hugeMap.put("key", "value");
		assertTrue(hugeMap.containsKey("key"));
		assertFalse(hugeMap.containsKey("value"));
	}

	@Test
	public void containsKeyLarge()
	{
		fillToThreshold();
		assertTrue(hugeMap.containsKey("3"));
		assertFalse(hugeMap.containsKey("value"));
	}

	@Test
	public void containsValue()
	{
		hugeMap.put("key", "value");
		assertTrue(hugeMap.containsValue("value"));
		assertFalse(hugeMap.containsValue("key"));
	}

	@Test
	public void containsValueLarge()
	{
		fillToThreshold();
		assertTrue(hugeMap.containsValue("3"));
		assertFalse(hugeMap.containsValue("key"));
	}

	@Test
	public void entrySet()
	{
		hugeMap.put("key", "value");
		assertEquals(hugeMap.entrySet().size(), 1);
	}

	@Test
	public void entrySetLarge()
	{
		fillToThreshold();
		assertEquals(hugeMap.entrySet().size(), HugeMap.THRESHOLD);
	}

	@Test
	public void get()
	{
		hugeMap.put("key", "value");
		assertEquals(hugeMap.get("key"), "value");
		assertNull(hugeMap.get("value"));
	}

	@Test
	public void getLarge()
	{
		fillToThreshold();
		assertEquals(hugeMap.get("2"), "2");
		assertNull(hugeMap.get("value"));
	}

	@Test
	public void isEmpty()
	{
		assertTrue(hugeMap.isEmpty());
		hugeMap.put("key", "value");
		assertFalse(hugeMap.isEmpty());
	}

	@Test
	public void isEmptyLarge()
	{
		assertTrue(hugeMap.isEmpty());
		fillToThreshold();
		assertFalse(hugeMap.isEmpty());
	}

	@Test
	public void keySet()
	{
		hugeMap.put("key", "value");
		assertEquals(hugeMap.keySet(), Sets.newHashSet("key"));
	}

	@Test
	public void keySetLarge()
	{
		fillToThreshold();
		assertEquals(hugeMap.keySet().size(), HugeMap.THRESHOLD);
	}

	@Test
	public void putAll()
	{
		hugeMap.putAll(Collections.singletonMap("key", "value"));
		assertEquals(hugeMap.size(), 1);
	}

	@Test
	public void putAllLarge()
	{
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < HugeMap.THRESHOLD; i++)
		{
			map.put(Integer.toString(i), Integer.toString(i));
		}

		hugeMap.putAll(map);
		assertEquals(hugeMap.size(), HugeMap.THRESHOLD);
	}

	@Test
	public void remove()
	{
		fillToThreshold();
		assertEquals(hugeMap.remove("5"), "5");
		assertEquals(hugeMap.size(), HugeMap.THRESHOLD - 1);
	}

	@Test
	public void removeLarge()
	{
		hugeMap.put("test", "value");
		assertEquals(hugeMap.remove("test"), "value");
		assertTrue(hugeMap.isEmpty());
	}

	@Test
	public void values()
	{
		hugeMap.put("key", "value");
		assertEquals(hugeMap.values().size(), 1);
	}

	@Test
	public void valuesLarge()
	{
		fillToThreshold();
		assertEquals(hugeMap.values().size(), HugeMap.THRESHOLD);
	}

	private void fillToThreshold()
	{
		IntStream.range(0, HugeMap.THRESHOLD).mapToObj(Integer::toString).forEach(s -> hugeMap.put(s, s));
	}
}
