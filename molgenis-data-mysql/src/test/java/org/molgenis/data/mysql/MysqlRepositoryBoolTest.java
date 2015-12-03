package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

/** Test for MolgenisFieldTypes.BOOL */
public class MysqlRepositoryBoolTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData varcharMD = new DefaultEntityMetaData("BoolTest").setLabel("Bool Test");
		varcharMD.setIdAttribute("col1");
		varcharMD.addAttribute("col1").setDataType(MolgenisFieldTypes.BOOL).setNillable(false);
		varcharMD.addAttribute("col2").setDataType(MolgenisFieldTypes.BOOL);
		varcharMD.addAttribute("col3").setDataType(MolgenisFieldTypes.BOOL);
		return varcharMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `BoolTest`(`col1` BOOL NOT NULL, `col2` BOOL, `col3` BOOL, PRIMARY KEY (`col1`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity createTestEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", false);
		e.set("col2", false);
		e.set("col3", true);
		return e;
	}

	@Override
	@Test
	public void test() throws Exception
	{
		super.test();
	}

	@Override
	public void verifyTestEntity(Entity e)
	{
		assertEquals(e.getBoolean("col1"), Boolean.FALSE);
		assertEquals(e.get("col2"), Boolean.FALSE);
		assertEquals(e.get("col3"), Boolean.TRUE);
	}
}
