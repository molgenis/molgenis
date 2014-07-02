package org.molgenis.data.mysql;

import org.molgenis.MolgenisFieldTypes;
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
		DefaultEntityMetaData varcharMD = new DefaultEntityMetaData("DecimalTest").setLabel("Decimal Test").setIdAttribute("col1");
		varcharMD.addAttribute("col1").setDataType(MolgenisFieldTypes.DECIMAL).setNillable(false);
		varcharMD.addAttribute("col2").setDataType(MolgenisFieldTypes.DECIMAL);
		varcharMD.addAttribute("col3").setDataType(MolgenisFieldTypes.DECIMAL).setDefaultValue(1.3);
		return varcharMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `DecimalTest`(`col1` DOUBLE(65,30) NOT NULL, `col2` DOUBLE(65,30), `col3` DOUBLE(65,30), PRIMARY KEY (`col1`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity defaultEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", 2.9);
		e.set("col2", 3.1);
		return e;
	}
}
