package org.molgenis.data.support;

import static org.testng.Assert.assertNotEquals;

import java.util.Collections;

import org.testng.annotations.Test;

public class MapEntityTest
{
	// regression test for: https://github.com/molgenis/molgenis/issues/3534
	@Test
	public void hashCode_()
	{
		MapEntity entity = new MapEntity(Collections.singletonMap("key", "value"));
		MapEntity otherEntity = new MapEntity(Collections.singletonMap("key", "otherValue"));
		assertNotEquals(entity.hashCode(), otherEntity.hashCode());
	}
}
