package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.util.Date;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.util.MolgenisDateFormat;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

public abstract class AbstractDateDatatypeIT extends AbstractDatatypeIT
{
	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("DateTest");
		entityMetaData.addAttribute("col1", ROLE_ID).setDataType(DATE).setNillable(false);
		entityMetaData.addAttribute("col2").setDataType(DATE);
		entityMetaData.addAttribute("col3").setDataType(DATE);

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("col1", parseDate("2012-03-13"));
		entity.set("col2", parseDate("2012-03-14"));
		entity.set("col3", parseDate("1992-03-13"));
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.get("col1"), parseDate("2012-03-13"));
		assertEquals(entity.getDate("col2"), parseDate("2012-03-14"));
		assertEquals(entity.getUtilDate("col3"), parseDate("1992-03-13"));
	}

	@Override
	public void updateTestEntity(Entity entity) throws Exception
	{
		entity.set("col2", parseDate("2000-03-14"));
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		entity.set("col1", parseDate("2012-03-13"));
		entity.set("col2", parseDate("2000-03-14"));
		entity.set("col3", parseDate("1992-03-13"));
	}

	private Date parseDate(String date) throws ParseException
	{
		return MolgenisDateFormat.getDateFormat().parse(date);
	}
}
