package org.molgenis.data.mysql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

/** Test for MolgenisFieldTypes.COMPOUND */
public class MysqlRepositoryCompoundTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		DefaultEntityMetaData compoundMD = new DefaultEntityMetaData("compound").setAbstract(true);
		compoundMD.addAttribute("col2").setDataType(MolgenisFieldTypes.BOOL);

		DefaultEntityMetaData rootMD = new DefaultEntityMetaData("CompoundTest").setLabel("CompoundTest");
		rootMD.setIdAttribute("col1");
		rootMD.addAttribute("col1").setDataType(MolgenisFieldTypes.BOOL).setNillable(false);
		rootMD.addAttribute("compound").setDataType(MolgenisFieldTypes.COMPOUND).setRefEntity(compoundMD);
		rootMD.addAttribute("col3").setDataType(MolgenisFieldTypes.BOOL).setDefaultValue(true);

		return rootMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `CompoundTest`(`col1` BOOL NOT NULL, `col2` BOOL, `col3` BOOL, PRIMARY KEY (`col1`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity defaultEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", false);
		e.set("col2", false);
		return e;
	}

	@Override
	public void test() throws Exception
	{
		coll.drop("CompoundTest");
		super.test();
	}

}
