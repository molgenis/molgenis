package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class AbstractDatetimeDatatypeIT extends AbstractDatatypeIT
{
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("DatetimeTest");
		entityMetaData.addAttribute("col1", ROLE_ID).setDataType(DATETIME).setNillable(false);
		entityMetaData.addAttribute("col2").setDataType(DATETIME);
		entityMetaData.addAttribute("col3").setDataType(DATETIME);// .setDefaultValue("2010-09-29T18:46:19UCT"); (see
																	// issue #4554)

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("col1", sdf.parse("2012-03-13 23:59:33"));
		entity.set("col2", sdf.parse("2013-02-09 13:12:11"));
		assertEquals(entity.getUtilDate("col1"), sdf.parse("2012-03-13 23:59:33"));
		assertEquals(entity.getUtilDate("col2"), sdf.parse("2013-02-09 13:12:11"));
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.getUtilDate("col1"), sdf.parse("2012-03-13 23:59:33"));
		assertEquals(entity.getUtilDate("col2"), sdf.parse("2013-02-09 13:12:11"));
		// assertEquals(entity.getUtilDate("col3"), sdf.parse("2010-09-29 18:46:19")); (see issue #4554)
	}

	@Override
	public void updateTestEntity(Entity entity) throws Exception
	{
		entity.set("col2", sdf.parse("2013-02-09 13:00:00"));
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		assertEquals(entity.getUtilDate("col1"), sdf.parse("2012-03-13 23:59:33"));
		assertEquals(entity.getUtilDate("col2"), sdf.parse("2013-02-09 13:00:00"));
	}

	// Delete and update do not work for datetime, this is a bug and should be fixed see
	// https://github.com/molgenis/molgenis/issues/4322
	@Override
	protected void delete(Entity entity)
	{
	}

	@Override
	protected void verifyEntityDeleted(Entity entity)
	{
	}

	@Override
	protected void update(Entity entity)
	{
	}

	@Override
	protected void verifyEntityUpdated(Entity entity) throws Exception
	{
	}

}
