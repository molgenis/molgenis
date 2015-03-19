package org.molgenis.util;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

public class EntityUtilsTest
{
	private static DefaultEntityMetaData entityMetaData;

	@Test
	public void isEmpty()
	{
		assertTrue(EntityUtils.isEmpty(new MapEntity()));
		assertTrue(EntityUtils.isEmpty(new MapEntity("col", null)));
		assertFalse(EntityUtils.isEmpty(new MapEntity("col", "test")));
	}

	@Test
	public void convert()
	{
		entityMetaData = new DefaultEntityMetaData("User");
		entityMetaData.addAttribute("name").setIdAttribute(true).setNillable(false);

		DataService dataService = mock(DataService.class);

		User user = new User(dataService);
		user.setName("Piet");
		User converted = EntityUtils.convert(user, User.class, dataService);
		assertEquals(converted, user);

		Entity entity = new DefaultEntity(entityMetaData, dataService);
		entity.set("name", "Piet");
		converted = EntityUtils.convert(entity, User.class, dataService);
		assertEquals(converted, entity);

	}

	public static class User extends DefaultEntity
	{
		private static final long serialVersionUID = 1L;

		public User(DataService dataService)
		{
			super(entityMetaData, dataService);
		}

		public String getName()
		{
			return getString("name");
		}

		public void setName(String name)
		{
			set("name", name);
		}
	}

}
