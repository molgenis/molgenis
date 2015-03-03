package org.molgenis.data;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

public class AutoIdEntityIterableDecoratorTest
{
	@Test
	public void iterator()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("test");
		entityMetaData.addAttribute("id").setIdAttribute(true).setNillable(false).setAuto(true);

		List<Entity> entities = Arrays.asList(new MapEntity("id"), new MapEntity("id"), new MapEntity("id"));
		Map<Integer, Object> ids = new HashMap<>();

		AutoIdEntityIterableDecorator decorator = new AutoIdEntityIterableDecorator(entityMetaData, entities,
				new UuidGenerator(), ids);

		// First time
		assertEquals(Iterables.size(decorator), entities.size());
		int i = 0;
		for (Entity entity : decorator)
		{
			assertEquals(entity.getIdValue(), ids.get(i++));
		}

		// Second time
		assertEquals(Iterables.size(decorator), entities.size());
		i = 0;
		for (Entity entity : decorator)
		{
			assertEquals(entity.getIdValue(), ids.get(i++));
		}
	}
}
