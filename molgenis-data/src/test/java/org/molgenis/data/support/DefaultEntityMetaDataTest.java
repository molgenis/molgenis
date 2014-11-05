package org.molgenis.data.support;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;

public class DefaultEntityMetaDataTest
{
	@Test
	public void DefaultEntityMetaDataEntityMetaData()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.setAbstract(true);
		entityMetaData.setDescription("description");
		entityMetaData.setIdAttribute("id");
		entityMetaData.setLabel("label");
		entityMetaData.setLabelAttribute("labelAttribute");
		entityMetaData.addAttribute("labelAttribute").setDescription("label attribute");
		entityMetaData.addAttribute("id").setDescription("id attribute");
		assertEquals(new DefaultEntityMetaData(entityMetaData), entityMetaData);
	}

	@Test
	public void getAttributesExtends()
	{
		// Verify that in case EntityMetaData extends other EntityMetaData only the attributes of this EntityMetaData
		// are returned.
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		DefaultAttributeMetaData attr = entityMetaData.addAttribute("attr");
		DefaultEntityMetaData baseEntityMetaData = new DefaultEntityMetaData("baseEntity");
		baseEntityMetaData.addAttribute("baseAttr");
		entityMetaData.setExtends(baseEntityMetaData);
		assertEquals(entityMetaData.getAttributes(), Collections.singletonList(attr));
	}
}
