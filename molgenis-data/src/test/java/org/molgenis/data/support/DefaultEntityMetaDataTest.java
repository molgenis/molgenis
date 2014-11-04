package org.molgenis.data.support;

import static org.testng.Assert.assertEquals;

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
}
