package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Collections;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MapEntityTest
{
	Entity entity;

	@BeforeClass
	public void setUpBeforeClass()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("entity");
		emd.addAttribute("id", ROLE_ID);
		emd.addAttribute("attr");
		emd.addAttribute("defaultAttr").setDataType(STRING).setDefaultValue("DEFAULT VALUE");

		entity = new DefaultEntity(emd, mock(DataService.class));
		entity.set("id", "0");
		entity.set("attr", "test");
	}

	// regression test for: https://github.com/molgenis/molgenis/issues/3534
	@Test
	public void hashCode_()
	{
		MapEntity entity = new MapEntity(Collections.singletonMap("key", "value"));
		MapEntity otherEntity = new MapEntity(Collections.singletonMap("key", "otherValue"));
		assertNotEquals(entity.hashCode(), otherEntity.hashCode());
	}

	@Test
	public void newMapEntityOther()
	{
		MapEntity mapEntity = new MapEntity(entity);

		assertEquals(mapEntity.get("id"), "0");
		assertEquals(mapEntity.get("attr"), "test");
		assertEquals(mapEntity.get("defaultAttr"), "DEFAULT VALUE");
	}

	@Test
	public void newMapEntityMetaData()
	{
		MapEntity mapEntity = new MapEntity(entity.getEntityMetaData());

		assertEquals(mapEntity.get("defaultAttr"), "DEFAULT VALUE");
	}

	@Test
	public void newMapEntityOtherMetaData()
	{
		MapEntity mapEntity = new MapEntity(entity, entity.getEntityMetaData());

		assertEquals(mapEntity.get("id"), "0");
		assertEquals(mapEntity.get("attr"), "test");
		assertEquals(mapEntity.get("defaultAttr"), "DEFAULT VALUE");
	}
}
