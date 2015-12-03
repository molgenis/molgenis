package org.molgenis.data.mysql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.Assert;

/** Test for MolgenisFieldTypes.INT */
public class MysqlRepositoryIntTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData varcharMD = new DefaultEntityMetaData("IntegerTest").setLabel("Integer Test");
		varcharMD.setIdAttribute("col1");
		varcharMD.addAttribute("col1").setDataType(MolgenisFieldTypes.INT).setNillable(false);
		varcharMD.addAttribute("col2").setDataType(MolgenisFieldTypes.INT);
		varcharMD.addAttribute("col3").setDataType(MolgenisFieldTypes.INT);
		return varcharMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `IntegerTest`(`col1` INTEGER NOT NULL, `col2` INTEGER, `col3` INTEGER, PRIMARY KEY (`col1`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity createTestEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", 1);
		e.set("col2", 3);
		e.set("col3", -3);
		return e;
	}

	@Override
	public void verifyTestEntity(Entity e) throws Exception
	{
		Assert.assertEquals(e.get("col1"), 1);
		Assert.assertEquals(e.get("col2"), 3);
		Assert.assertEquals(e.get("col3"), -3);
	}
}
