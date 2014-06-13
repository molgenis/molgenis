package org.molgenis.data.mysql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

/** Test for MolgenisFieldTypes.COMPOUND */
public class MysqlRepositoryExtendsTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		DefaultEntityMetaData superclass2 = new DefaultEntityMetaData("super").setAbstract(true).setIdAttribute("col1");
		superclass2.addAttribute("col1").setDataType(MolgenisFieldTypes.BOOL).setNillable(false);

		DefaultEntityMetaData superclass = new DefaultEntityMetaData("ExtendsTest").setExtends(superclass2);
		superclass.addAttribute("col2").setDataType(MolgenisFieldTypes.BOOL);

		DefaultEntityMetaData subclass = new DefaultEntityMetaData("ExtendsTest").setLabel("Extends Test").setExtends(
				superclass);
		subclass.addAttribute("col3").setDataType(MolgenisFieldTypes.BOOL).setDefaultValue(true);

		return subclass;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `ExtendsTest`(`col1` BOOL NOT NULL, `col2` BOOL, `col3` BOOL, PRIMARY KEY (`col1`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity defaultEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", false);
		e.set("col2", false);
		return e;
	}
}
