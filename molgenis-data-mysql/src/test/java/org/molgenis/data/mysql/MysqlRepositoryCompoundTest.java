package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

/** Test for MolgenisFieldTypes.COMPOUND */
public class MysqlRepositoryCompoundTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		DefaultAttributeMetaData attributePart = new DefaultAttributeMetaData("col2")
				.setDataType(MolgenisFieldTypes.BOOL);

		EditableEntityMetaData rootMD = new DefaultEntityMetaData("CompoundTest").setLabel("CompoundTest");
		rootMD.setIdAttribute("col1");
		rootMD.addAttribute("col1").setDataType(MolgenisFieldTypes.BOOL).setNillable(false);
		rootMD.addAttribute("compound").setDataType(MolgenisFieldTypes.COMPOUND)
				.setAttributesMetaData(Arrays.<AttributeMetaData> asList(attributePart));
		rootMD.addAttribute("col3").setDataType(MolgenisFieldTypes.BOOL);

		return rootMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `CompoundTest`(`col1` BOOL NOT NULL, `col2` BOOL, `col3` BOOL, PRIMARY KEY (`col1`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity createTestEntity()
	{
		Entity e = new MapEntity();
		e.set("col1", false);
		e.set("col2", false);
		e.set("col3", true);
		return e;
	}

	@Override
	public void verifyTestEntity(Entity e)
	{
		assertEquals(e.getBoolean("col1"), Boolean.FALSE);
		assertEquals(e.get("col2"), Boolean.FALSE);
		assertEquals(e.get("col3"), Boolean.TRUE);
	}

	@Override
	public void test() throws Exception
	{
		metaDataService.deleteEntityMeta("CompoundTest");
		super.test();
	}

}
