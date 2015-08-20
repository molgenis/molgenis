package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

/** Test for MolgenisFieldTypes.DECIMAL */
public class MysqlRepositoryDecimalTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData varcharMD = new DefaultEntityMetaData("DecimalTest").setLabel("Decimal Test");
		varcharMD.setIdAttribute("col1");
		varcharMD.addAttribute("col1").setDataType(MolgenisFieldTypes.DECIMAL).setNillable(false);
		varcharMD.addAttribute("col2").setDataType(MolgenisFieldTypes.DECIMAL);
		varcharMD.addAttribute("col3").setDataType(MolgenisFieldTypes.DECIMAL);
		return varcharMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `DecimalTest`(`col1` DOUBLE NOT NULL, `col2` DOUBLE, `col3` DOUBLE, PRIMARY KEY (`col1`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity createTestEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", 2.9);
		e.set("col2", 3.1);
		e.set("col3", 5);
		return e;
	}

	@Override
	public void verifyTestEntity(Entity e) throws Exception
	{
		assertEquals(e.get("col1"), 2.9);
		assertEquals(e.get("col2"), 3.1);
		assertEquals(e.get("col3"), 5.0);
	}
}
