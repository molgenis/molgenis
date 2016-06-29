package org.molgenis.integrationtest.data.abstracts.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;

import java.text.SimpleDateFormat;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class AbstractDatetimeDatatypeIT extends AbstractDatatypeIT
{
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	@Override
	public EntityMetaData createMetaData()
	{
		EntityMetaData entityMetaData = null; //new EntityMetaData("DatetimeTest");
		//		entityMetaData.addAttribute("col1", ROLE_ID).setDataType(DATE_TIME).setNillable(false); // FIXME
		//		entityMetaData.addAttribute("col2").setDataType(DATE_TIME);
		//		entityMetaData.addAttribute("col3").setDataType(DATE_TIME).setDefaultValue("01-01-2014");

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
