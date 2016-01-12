package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;

public abstract class AbstractDateDatatypeTest extends AbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("DateTest");
		entityMetaData.addAttribute("col1").setDataType(DATE).setIdAttribute(true).setNillable(false);
		entityMetaData.addAttribute("col2").setDataType(DATE);
		entityMetaData.addAttribute("col3").setDataType(DATE);

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(DefaultEntity entity) throws Exception
	{
		entity.set("col1", "2012-03-13");
		entity.set("col2", "2012-03-14");
		entity.set("col3", "1992-03-13");
	}

	@Override
	public void verifyTestEntity(Entity entity) throws Exception
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		assertEquals(entity.get("col1"), new java.sql.Date(sdf.parse("2012-03-13").getTime()));
		assertEquals(entity.get("col2"), new java.sql.Date(sdf.parse("2012-03-14").getTime()));
		assertEquals(entity.getUtilDate("col3"), sdf.parse("1992-03-13"));
	}

}
