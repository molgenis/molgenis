package org.molgenis.util;

import static org.mockito.Mockito.mock;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

public class EntityUtilsTest
{
	private static EntityMetaData entityMetaData;

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
		entityMetaData = new EntityMetaData("User");
		entityMetaData.addAttribute("name", ROLE_ID);

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

	@Test
	public void doesExtend()
	{
		EntityMetaData grandfather = new EntityMetaData("grandfather");
		assertFalse(EntityUtils.doesExtend(grandfather, "grandfather"));

		EntityMetaData father = new EntityMetaData("father");
		father.setExtends(grandfather);
		assertTrue(EntityUtils.doesExtend(father, "grandfather"));

		EntityMetaData child = new EntityMetaData("child");
		child.setExtends(father);
		assertTrue(EntityUtils.doesExtend(child, "grandfather"));
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
