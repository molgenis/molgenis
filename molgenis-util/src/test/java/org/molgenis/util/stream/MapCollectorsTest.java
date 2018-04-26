package org.molgenis.util.stream;

import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static org.testng.Assert.assertEquals;

public class MapCollectorsTest
{
	@Test
	public void testToLinkedMap() throws Exception
	{
		Map<String, Integer> expectedLinkedMap = new LinkedHashMap<>();
		expectedLinkedMap.put("a", 1);
		expectedLinkedMap.put("bb", 2);
		expectedLinkedMap.put("ccc", 3);
		Map<String, Integer> actualLinkedMap = Stream.of("a", "bb", "ccc")
													 .collect(MapCollectors.toLinkedMap(identity(), String::length));
		assertEquals(expectedLinkedMap, actualLinkedMap);
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Duplicate key detected with values '1' and '2'")
	public void testToLinkedMapDuplicateKey() throws Exception
	{
		//noinspection ResultOfMethodCallIgnored
		Stream.of("a1", "a2").collect(MapCollectors.toLinkedMap(str -> str.charAt(0), str -> str.charAt(1)));
	}
}