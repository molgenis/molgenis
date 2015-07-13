package org.molgenis.data.mysql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

/** Test for MolgenisFieldTypes.COMPOUND */
public class MysqlRepositoryExtendsTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	@Test
	public void test() throws Exception
	{
		super.test();
	}

	@Override
	public EntityMetaData createMetaData()
	{
		EditableEntityMetaData superclass2 = new DefaultEntityMetaData("super0").setAbstract(true);
		superclass2.addAttribute("col1").setDataType(MolgenisFieldTypes.BOOL).setNillable(false).setIdAttribute(true);
		metaDataService.addEntityMeta(superclass2);

		EditableEntityMetaData superclass = new DefaultEntityMetaData("super1").setExtends(superclass2).setAbstract(
				true);
		superclass.addAttribute("col2").setDataType(MolgenisFieldTypes.BOOL);
		metaDataService.addEntityMeta(superclass);

		EditableEntityMetaData subclass = new DefaultEntityMetaData("ExtendsTest").setLabel("Extends Test").setExtends(
				superclass);
		subclass.addAttribute("col3").setDataType(MolgenisFieldTypes.BOOL).setDefaultValue(true);
		metaDataService.addEntityMeta(subclass);

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
