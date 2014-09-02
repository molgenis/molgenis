package org.molgenis.data.mysql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/** Test for MolgenisFieldTypes.XREF */
public class MysqlRepositoryXrefTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Autowired
	MysqlRepositoryCollection coll;

	@Override
	public EntityMetaData createMetaData()
	{
		DefaultEntityMetaData refEntity = new DefaultEntityMetaData("StringTarget");
		refEntity.setLabelAttribute("label");
		refEntity.setIdAttribute("identifier");
		refEntity.addAttribute("identifier").setNillable(false);

		DefaultEntityMetaData refEntity2 = new DefaultEntityMetaData("IntTarget");
		refEntity2.setIdAttribute("identifier");
		refEntity2.addAttribute("identifier").setDataType(MolgenisFieldTypes.INT).setNillable(false);

		DefaultEntityMetaData xrefEntity = new DefaultEntityMetaData("XrefTest").setLabel("Xref Test");
		xrefEntity.setIdAttribute("identifier");
		xrefEntity.addAttribute("identifier").setNillable(false);
		xrefEntity.addAttribute("stringRef").setDataType(MolgenisFieldTypes.XREF).setRefEntity(refEntity)
				.setNillable(false);
		xrefEntity.addAttribute("intRef").setDataType(MolgenisFieldTypes.XREF).setRefEntity(refEntity2);
		return xrefEntity;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `XrefTest`(`identifier` VARCHAR(255) NOT NULL, `stringRef` VARCHAR(255) NOT NULL, `intRef` INTEGER, PRIMARY KEY (`identifier`)) ENGINE=InnoDB;";
	}

	@Override
	public Entity defaultEntity()
	{
		return null;
	}

	@Override
	@Test
	public void test() throws Exception
	{
		coll.dropEntityMetaData(getMetaData().getName());
		coll.dropEntityMetaData(getMetaData().getAttribute("stringRef").getRefEntity().getName());
		coll.dropEntityMetaData(getMetaData().getAttribute("intRef").getRefEntity().getName());

		// create
		MysqlRepository stringRepo = coll.add(getMetaData().getAttribute("stringRef").getRefEntity());
		MysqlRepository intRepo = coll.add(getMetaData().getAttribute("intRef").getRefEntity());
		MysqlRepository xrefRepo = coll.add(getMetaData());

		Assert.assertEquals(xrefRepo.getCreateSql(), createSql());

		Assert.assertEquals(xrefRepo.getCreateFKeySql().get(0),
				"ALTER TABLE XrefTest ADD FOREIGN KEY (`stringRef`) REFERENCES `StringTarget`(`identifier`)");

		xrefRepo.drop();
		stringRepo.drop();
		intRepo.drop();

		Assert.assertEquals(xrefRepo.getCreateSql(), createSql());
		stringRepo.create();
		intRepo.create();
		xrefRepo.create();

		// add records
		Entity entity = new MapEntity();
		entity.set("identifier", "ref1");
		stringRepo.add(entity);

		entity.set("identifier", "ref2");
		stringRepo.add(entity);

		entity.set("identifier", "ref3");
		stringRepo.add(entity);

		entity.set("identifier", 1);
		intRepo.add(entity);

		entity.set("identifier", 2);
		intRepo.add(entity);

		entity.set("identifier", "one");
		entity.set("stringRef", "ref1");
		entity.set("intRef", 1);
		xrefRepo.add(entity);

		entity.set("identifier", "two");
		entity.set("stringRef", "ref2");
		entity.set("intRef", null);
		xrefRepo.add(entity);

		entity.set("identifier", "three");
		entity.set("stringRef", "ref3");
		entity.set("intRef", 2);
		xrefRepo.add(entity);

		Assert.assertEquals(xrefRepo.getSelectSql(new QueryImpl(), Lists.newArrayList()),
				"SELECT this.`identifier`, this.`stringRef`, this.`intRef` FROM `XrefTest` AS this");

		for (Entity e : xrefRepo)
		{
			logger.debug(e);

			Assert.assertNotNull(e.getEntity("stringRef"));
			Assert.assertEquals(e.getEntity("stringRef").get("identifier"), "ref1");
			Assert.assertEquals(e.get("stringRef"), "ref1");
			Assert.assertEquals(e.get("intRef"), 1);
			break;
		}

		// verify not null error

		// verify default
	}
}
