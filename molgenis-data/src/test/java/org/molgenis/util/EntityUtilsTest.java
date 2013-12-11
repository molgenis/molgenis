package org.molgenis.util;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

public class EntityUtilsTest
{

	@Test
	public void isEmpty()
	{
		assertTrue(EntityUtils.isEmpty(new MapEntity()));
		assertTrue(EntityUtils.isEmpty(new MapEntity("col", null)));
		assertFalse(EntityUtils.isEmpty(new MapEntity("col", "test")));
	}
}
