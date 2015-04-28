package org.molgenis.data.mysql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

/** Test for MolgenisFieldTypes.STRING */
public class MysqlRepositoryStringTest extends MysqlRepositoryAbstractDatatypeTest
{

	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData varcharMD = new DefaultEntityMetaData("VarcharTest").setLabel("Varchar Test");
		varcharMD.setIdAttribute("col1");
		varcharMD.addAttribute("col1").setDataType(MolgenisFieldTypes.STRING).setNillable(false);
		varcharMD.addAttribute("col2").setDataType(MolgenisFieldTypes.STRING);
		varcharMD.addAttribute("col3").setDataType(MolgenisFieldTypes.STRING).setDefaultValue("myDefault");
		return varcharMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `VarcharTest`(`col1` VARCHAR(255) NOT NULL, `col2` TEXT, `col3` TEXT, PRIMARY KEY (`col1`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity defaultEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", "col2value");
		e.set("col2", "col2value");
		return e;
	}
}
