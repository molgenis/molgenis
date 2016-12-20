package org.molgenis.data.populate;

import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class RandomIdGeneratorTest
{
	private final RandomIdGenerator randomIdGenerator = new RandomIdGenerator();

	@Test
	public void testGenerateId()
	{
		Set<String> ids = IntStream.range(0, 10000).mapToObj((x) -> randomIdGenerator.generateId())
				.collect(Collectors.toSet());
		assertTrue(ids.size() == 10000);
		assertTrue(ids.stream().allMatch(id -> id.length() == 26));
	}

	@Test
	public void testGenerateShortId()
	{
		Set<String> ids = IntStream.range(0, 1000).mapToObj((x) -> randomIdGenerator.generateShortId())
				.collect(Collectors.toSet());
		assertTrue(ids.size() == 1000);
		assertTrue(ids.stream().allMatch(id -> id.length() == 8));
	}

	@Test
	public void testGenerateLongId()
	{
		String id = randomIdGenerator.encodeRandomBytes(30);
		assertEquals(id.length(), 48);
	}
}
