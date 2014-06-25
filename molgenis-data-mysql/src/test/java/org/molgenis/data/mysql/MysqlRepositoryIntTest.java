package org.molgenis.data.mysql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

/** Test for MolgenisFieldTypes.INT */
public class MysqlRepositoryIntTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		DefaultEntityMetaData varcharMD = new DefaultEntityMetaData("IntegerTest").setLabel("Integer Test");
		varcharMD.setIdAttribute("col1");
		varcharMD.addAttribute("col1").setDataType(MolgenisFieldTypes.INT).setNillable(false);
		varcharMD.addAttribute("col2").setDataType(MolgenisFieldTypes.INT);
		varcharMD.addAttribute("col3").setDataType(MolgenisFieldTypes.INT).setDefaultValue(-1);
		return varcharMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `IntegerTest`(`col1` INTEGER NOT NULL, `col2` INTEGER, `col3` INTEGER, PRIMARY KEY (`col1`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity defaultEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", 1);
		e.set("col2", 1);
		return e;
	}
}
