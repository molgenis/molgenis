package org.molgenis.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

public class HugeMapTest
{
	private HugeMap<String, String> hugeMap;

	@BeforeMethod
	public void beforeMethod()
	{
		hugeMap = new HugeMap<String, String>();
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
	public void containsKey()
	{
		hugeMap.put("key", "value");
		assertTrue(hugeMap.containsKey("key"));
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
	public void entrySet()
	{
		hugeMap.put("key", "value");
		assertEquals(hugeMap.entrySet().size(), 1);
	}

	@Test
	public void get()
	{
		hugeMap.put("key", "value");
		assertEquals(hugeMap.get("key"), "value");
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
	public void keySet()
	{
		hugeMap.put("key", "value");
		assertEquals(hugeMap.keySet(), Sets.newHashSet("key"));
	}

	@Test
	public void putAll()
	{
		hugeMap.putAll(Collections.singletonMap("key", "value"));
		assertEquals(hugeMap.size(), 1);
	}

	@Test
	public void remove()
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
}
