package org.molgenis.data.mysql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

/** Test for MolgenisFieldTypes.DATETIME */
public class MysqlRepositoryDatetimeTest extends MysqlRepositoryAbstractDatatypeTest
{

	@Override
	public EntityMetaData createMetaData()
	{
		DefaultEntityMetaData varcharMD = new DefaultEntityMetaData("DatetimeTest").setLabel("Datetime Test").setIdAttribute("col1");
		varcharMD.addAttribute("col1").setDataType(MolgenisFieldTypes.DATETIME).setNillable(false);
		varcharMD.addAttribute("col2").setDataType(MolgenisFieldTypes.DATETIME);
		varcharMD.addAttribute("col3").setDataType(MolgenisFieldTypes.DATETIME)
				.setDefaultValue(new StringToDateConverter().convert("1992-03-13 24:33:00"));
		return varcharMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `DatetimeTest`(`col1` DATETIME NOT NULL, `col2` DATETIME, `col3` DATETIME, PRIMARY KEY (`col1`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity defaultEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", "2012-03-13 23:59:33");
		e.set("col2", "2012-03-14 23:00:22");
		return e;
	}
}
