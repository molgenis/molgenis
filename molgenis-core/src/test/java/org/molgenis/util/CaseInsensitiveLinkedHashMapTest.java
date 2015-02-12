package org.molgenis.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class CaseInsensitiveLinkedHashMapTest
{

	@Test
	public void put()
	{
		Map<String, Integer> map = new CaseInsensitiveLinkedHashMap<Integer>();

		map.put("Test_tje", 1);
		assertEquals(map.get("test_tje"), Integer.valueOf(1));

		map.put("test_tje", 2);
		assertEquals(map.get("test_Tje"), Integer.valueOf(2));
		assertEquals(map.size(), 1);
	}

	@Test
	public void keySet()
	{
		Map<String, Integer> map = new CaseInsensitiveLinkedHashMap<Integer>();
		map.put("Test", 1);
		assertEquals(map.keySet(), Sets.<String> newLinkedHashSet(Arrays.asList("Test")));

		map.put("test", 1);
		assertEquals(map.keySet(), Sets.<String> newLinkedHashSet(Arrays.asList("test")));

		map.put("Xtest", 2);
		assertEquals(map.keySet(), Sets.<String> newLinkedHashSet(Arrays.asList("test", "Xtest")));
	}

	@Test
	public void remove()
	{
		Map<String, Integer> map = new CaseInsensitiveLinkedHashMap<Integer>();
		map.put("Test", 1);
		map.remove("tesT");
		assertTrue(map.isEmpty());
	}

	@Test
	public void putAll()
	{
		Map<String, Integer> m = Maps.newLinkedHashMap();
		m.put("test", 1);
		m.put("Test", 2);
		m.put("XXX", 3);

		Map<String, Integer> map = new CaseInsensitiveLinkedHashMap<Integer>();
		map.putAll(m);

		assertEquals(map.get("TeST"), Integer.valueOf(2));
		assertEquals(map.get("xxx"), Integer.valueOf(3));
		assertEquals(map.size(), 2);
		assertEquals(map.keySet(), Sets.<String> newLinkedHashSet(Arrays.asList("Test", "XXX")));
	}

	@Test
	public void entrySet()
	{
		Map<String, Integer> map = new CaseInsensitiveLinkedHashMap<Integer>();
		map.put("1", 1);
		map.put("2", 2);
		map.put("3", 3);

		int i = 1;
		for (Map.Entry<String, Integer> entry : map.entrySet())
		{
			assertEquals(entry.getKey(), String.valueOf(i));
			assertEquals(entry.getValue(), Integer.valueOf(i));
			i++;
		}
	}
}
