package org.molgenis.data.mysql;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.EditableEntityMetaData;
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
	DataService dataService;

	@Override
	public EntityMetaData createMetaData()
	{
		DefaultEntityMetaData refEntity = new DefaultEntityMetaData("StringTarget");
		refEntity.addAttribute("identifier", ROLE_ID);
		refEntity.addAttribute("label", ROLE_LABEL);

		DefaultEntityMetaData refEntity2 = new DefaultEntityMetaData("IntTarget");
		refEntity2.addAttribute("identifier", ROLE_ID).setDataType(MolgenisFieldTypes.INT);

		EditableEntityMetaData xrefEntity = new DefaultEntityMetaData("XrefTest").setLabel("Xref Test");
		xrefEntity.addAttribute("identifier", ROLE_ID);
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
	public Entity createTestEntity()
	{
		return null;
	}

	@Override
	@Test
	public void test() throws Exception
	{
		// coll.deleteEntityMeta(getMetaData().getName());
		// coll.deleteEntityMeta(getMetaData().getAttribute("stringRef").getRefEntity().getName());
		// coll.deleteEntityMeta(getMetaData().getAttribute("intRef").getRefEntity().getName());

		// create
		MysqlRepository stringRepo = (MysqlRepository) dataService.getMeta()
				.addEntityMeta(getMetaData().getAttribute("stringRef").getRefEntity());
		MysqlRepository intRepo = (MysqlRepository) dataService.getMeta()
				.addEntityMeta(getMetaData().getAttribute("intRef").getRefEntity());
		MysqlRepository xrefRepo = (MysqlRepository) dataService.getMeta().addEntityMeta(getMetaData());

		Assert.assertEquals(xrefRepo.getCreateSql(), createSql());

		Assert.assertEquals(xrefRepo.getCreateFKeySql(getMetaData().getAttribute("stringRef")),
				"ALTER TABLE `XrefTest` ADD FOREIGN KEY (`stringRef`) REFERENCES `StringTarget`(`identifier`)");

		// simply dropping the repos won't work because the references keep existing after the tables are deleted, so we
		// use the data service
		dataService.getMeta().deleteEntityMeta(xrefRepo.getName());
		dataService.getMeta().deleteEntityMeta(stringRepo.getName());
		dataService.getMeta().deleteEntityMeta(intRepo.getName());

		Assert.assertEquals(xrefRepo.getCreateSql(), createSql());

		dataService.getMeta().addEntityMeta(getMetaData().getAttribute("intRef").getRefEntity());
		dataService.getMeta().addEntityMeta(getMetaData().getAttribute("stringRef").getRefEntity());
		dataService.getMeta().addEntityMeta(getMetaData());

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
			LOG.debug(e.toString());

			Assert.assertNotNull(e.getEntity("stringRef"));
			Assert.assertEquals(e.getEntity("stringRef").get("identifier"), "ref1");
			Assert.assertEquals(e.getEntity("intRef").get("identifier"), 1);
			break;
		}

		// verify not null error

		// verify default
	}
}
