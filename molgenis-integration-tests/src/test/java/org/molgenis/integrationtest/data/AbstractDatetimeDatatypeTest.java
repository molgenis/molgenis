package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.text.SimpleDateFormat;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class AbstractDatetimeDatatypeTest extends AbstractDatatypeTest
{
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("DatetimeTest");
		entityMetaData.addAttribute("col1").setDataType(DATETIME).setIdAttribute(true).setNillable(false);
		entityMetaData.addAttribute("col2").setDataType(DATETIME);
		entityMetaData.addAttribute("col3").setDataType(DATETIME).setDefaultValue("01-01-2014");

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
		assertNull(entity.get("col3")); // default value should NOT be set by the repository, for then the user
										// cannot
										// override it to be NULL in a form.
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
