package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

/** Test for MolgenisFieldTypes.DATE */
public class MysqlRepositoryDateTest extends MysqlRepositoryAbstractDatatypeTest
{

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("DateTest").setLabel("Date Test");
		entityMetaData.setIdAttribute("col1");
		entityMetaData.addAttribute("col1").setDataType(MolgenisFieldTypes.DATE).setNillable(false);
		entityMetaData.addAttribute("col2").setDataType(MolgenisFieldTypes.DATE);
		entityMetaData.addAttribute("col3").setDataType(MolgenisFieldTypes.DATE);
		return entityMetaData;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `DateTest`(`col1` DATE NOT NULL, `col2` DATE, `col3` DATE, PRIMARY KEY (`col1`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity createTestEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", "2012-03-13");
		e.set("col2", "2012-03-14");
		e.set("col3", "1992-03-13");
		return e;
	}

	@Override
	public void verifyTestEntity(Entity e) throws ParseException
	{
		assertEquals(e.get("col1"), new java.sql.Date(sdf.parse("2012-03-13").getTime()));
		assertEquals(e.get("col2"), new java.sql.Date(sdf.parse("2012-03-14").getTime()));
		assertEquals(e.getUtilDate("col3"), sdf.parse("1992-03-13"));
	}
}
