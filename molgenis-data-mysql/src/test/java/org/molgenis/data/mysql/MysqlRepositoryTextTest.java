package org.molgenis.data.mysql;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

/** Test for MolgenisFieldTypes.TEXT */
public class MysqlRepositoryTextTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData varcharMD = new DefaultEntityMetaData("TextTest").setLabel("Text Test");
		varcharMD.addAttribute("identifier", ROLE_ID).setDataType(MolgenisFieldTypes.INT).setAuto(true);
		varcharMD.addAttribute("col1").setDataType(MolgenisFieldTypes.TEXT).setNillable(false);
		varcharMD.addAttribute("col2").setDataType(MolgenisFieldTypes.TEXT);
		varcharMD.addAttribute("col3").setDataType(MolgenisFieldTypes.TEXT).setDefaultValue("lorem ipsum");
		return varcharMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `TextTest`(`identifier` INTEGER NOT NULL AUTO_INCREMENT, `col1` TEXT NOT NULL, `col2` TEXT, `col3` TEXT, PRIMARY KEY (`identifier`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity createTestEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", "lorem");
		e.set("col2", "ipsum");
		return e;
	}
}
