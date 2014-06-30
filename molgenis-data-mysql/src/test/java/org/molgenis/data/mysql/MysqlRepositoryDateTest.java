package org.molgenis.data.mysql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

/** Test for MolgenisFieldTypes.DATE */
public class MysqlRepositoryDateTest extends MysqlRepositoryAbstractDatatypeTest
{

	@Override
	public EntityMetaData createMetaData()
	{
		DefaultEntityMetaData varcharMD = new DefaultEntityMetaData("DateTest").setLabel("Date Test");
		varcharMD.setIdAttribute("col1");
		varcharMD.addAttribute("col1").setDataType(MolgenisFieldTypes.DATE).setNillable(false);
		varcharMD.addAttribute("col2").setDataType(MolgenisFieldTypes.DATE);
		varcharMD.addAttribute("col3").setDataType(MolgenisFieldTypes.DATE).setDefaultValue("1992-03-13");
		// TODO: better way to construct defaults, e.g. from formatted string "2014-04-03"
		return varcharMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `DateTest`(`col1` DATE NOT NULL, `col2` DATE, `col3` DATE, PRIMARY KEY (`col1`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity defaultEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", "2012-03-13");
		e.set("col2", "2012-03-14");
		return e;
	}
}
