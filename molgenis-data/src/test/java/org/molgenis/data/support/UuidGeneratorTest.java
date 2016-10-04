package org.molgenis.data.support;

import org.molgenis.data.populate.UuidGenerator;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;

public class UuidGeneratorTest
{
	private final UuidGenerator uuidGenerator = new UuidGenerator();

	@Test
	public void generateId()
	{
		Set<String> ids = new HashSet<>();
		for (int i = 0; i < 1000000; i++)
		{
			ids.add(uuidGenerator.generateId().toLowerCase());

		}
		assertEquals(ids.size(), 1000000);
	}
}
