package org.molgenis.data.mysql;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Test for MolgenisFieldTypes.XREF */
public class MysqlRepositoryXrefTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		DefaultEntityMetaData refEntity = new DefaultEntityMetaData("StringTarget").setLabelAttribute("label");
		refEntity.addAttribute("identifier").setNillable(false);

		DefaultEntityMetaData refEntity2 = new DefaultEntityMetaData("IntTarget");
		refEntity2.addAttribute("identifier").setDataType(MolgenisFieldTypes.INT).setNillable(false);

		DefaultEntityMetaData xrefEntity = new DefaultEntityMetaData("XrefTest").setLabel("Xref Test");
		xrefEntity.addAttribute("identifier").setNillable(false);
		xrefEntity.addAttribute("stringRef").setDataType(MolgenisFieldTypes.XREF).setRefEntity(refEntity)
				.setNillable(false);
		xrefEntity.addAttribute("intRef").setDataType(MolgenisFieldTypes.XREF).setRefEntity(refEntity2);
		return xrefEntity;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS XrefTest(identifier VARCHAR(255) NOT NULL, stringRef VARCHAR(255) NOT NULL, intRef INTEGER, PRIMARY KEY (identifier), FOREIGN KEY (stringRef) REFERENCES StringTarget(identifier), FOREIGN KEY (intRef) REFERENCES IntTarget(identifier)) ENGINE=InnoDB;";
	}

	@Override
	public Entity defaultEntity()
	{
		return null;
	}

	@Test
	public void test() throws Exception
	{
		// create
		MysqlRepository xrefRepo = new MysqlRepository(ds, getMetaData());
		Assert.assertEquals(xrefRepo.getCreateSql(), createSql());

		MysqlRepository stringRepo = new MysqlRepository(ds, getMetaData().getAttribute("stringRef").getRefEntity());
		MysqlRepository intRepo = new MysqlRepository(ds, getMetaData().getAttribute("intRef").getRefEntity());

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

		Assert.assertEquals(xrefRepo.getSelectSql(new QueryImpl()),
				"SELECT this.identifier, this.stringRef, this.intRef FROM XrefTest AS this");

		// "SElECT stringRef.identifier AS stringRef, XrefTest.intRef FROM XrefTest LEFT JOIN StringTarget AS stringRef ON (XrefTest.stringRef = stringRef.identifier) LEFT JOIN IntTarget AS intRef ON (XrefTest.intRef = intRef.identifier");

		for (Entity e : xrefRepo)
		{
			logger.debug(e);
			// Assert.assertEquals(e.get("stringRef"), "ref1");
			// Assert.assertEquals(e.get("intRef"), 1);
			// break;
		}

		// verify not null error

		// verify default
	}
}
