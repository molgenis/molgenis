package org.molgenis.util;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.testng.Assert.assertEquals;

public class ListEscapeUtilsTest
{

	@Test
	public void toListString()
	{
		assertEquals(ListEscapeUtils.toList("a"), Collections.singletonList("a"));
		assertEquals(ListEscapeUtils.toList("a,b,c"), Arrays.asList("a", "b", "c"));

		assertEquals(ListEscapeUtils.toList("\\,"), Collections.singletonList(","));
		assertEquals(ListEscapeUtils.toList("a\\,b"), Collections.singletonList("a,b"));

		assertEquals(ListEscapeUtils.toList("\\\\"), Collections.singletonList("\\"));
		assertEquals(ListEscapeUtils.toList("a\\\\b"), Collections.singletonList("a\\b"));

		assertEquals(ListEscapeUtils.toList("a,b,"), Arrays.asList("a", "b", ""));
		assertEquals(ListEscapeUtils.toList("a,,c"), Arrays.asList("a", "", "c"));
		assertEquals(ListEscapeUtils.toList(",b,c"), Arrays.asList("", "b", "c"));

		assertEquals(ListEscapeUtils.toList(""), Collections.emptyList());
		assertEquals(ListEscapeUtils.toList(null), null);
	}

	@Test
	public void toListStringcharchar()
	{
		assertEquals(ListEscapeUtils.toString(Arrays.asList("a", "b", "c"), ',', '/'), "a,b,c");
		assertEquals(ListEscapeUtils.toString(Collections.singletonList("a,b,c"), ',', '/'), "a/,b/,c");
		assertEquals(ListEscapeUtils.toString(Collections.singletonList("/"), ',', '/'), "//");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void toListStringcharchar_exception()
	{
		ListEscapeUtils.toList("", 'a', 'a');
	}

	@Test
	public void toStringList()
	{
		assertEquals("a", ListEscapeUtils.toString(Collections.singletonList("a")));
		assertEquals("a,b,c", ListEscapeUtils.toString(Arrays.asList("a", "b", "c")));

		assertEquals("\\,", ListEscapeUtils.toString(Collections.singletonList(",")));
		assertEquals("a\\,b", ListEscapeUtils.toString(Collections.singletonList("a,b")));

		assertEquals("\\\\", ListEscapeUtils.toString(Collections.singletonList("\\")));
		assertEquals("a\\\\b", ListEscapeUtils.toString(Collections.singletonList("a\\b")));

		assertEquals("a,b,", ListEscapeUtils.toString(Arrays.asList("a", "b", "")));
		assertEquals("a,,c", ListEscapeUtils.toString(Arrays.asList("a", "", "c")));
		assertEquals(",b,c", ListEscapeUtils.toString(Arrays.asList("", "b", "c")));

		assertEquals("a,b,", ListEscapeUtils.toString(Arrays.asList("a", "b", null)));
		assertEquals("a,,c", ListEscapeUtils.toString(Arrays.asList("a", null, "c")));
		assertEquals(",b,c", ListEscapeUtils.toString(Arrays.asList(null, "b", "c")));

		assertEquals("", ListEscapeUtils.toString(Collections.emptyList()));
		assertEquals(null, ListEscapeUtils.toString(null));
	}

	@Test
	public void toStringListcharchar()
	{
		assertEquals("a,b,c", ListEscapeUtils.toString(Arrays.asList("a", "b", "c"), ',', '/'));
		assertEquals("a/,b/,c", ListEscapeUtils.toString(Collections.singletonList("a,b,c"), ',', '/'));
		assertEquals("//", ListEscapeUtils.toString(Collections.singletonList("/"), ',', '/'));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void toStringListcharchar_exception()
	{
		ListEscapeUtils.toString(Collections.emptyList(), 'a', 'a');
	}
}
