package org.molgenis.data.mysql;

import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/** Test for MolgenisFieldTypes.MREF */
public class MysqlRepositoryMrefTest extends MysqlRepositoryAbstractDatatypeTest
{
	@Override
	public EntityMetaData createMetaData()
	{
		DefaultEntityMetaData refEntity = new DefaultEntityMetaData("StringTarget2");
		refEntity.setLabelAttribute("label");
		refEntity.setIdAttribute("identifier");
		refEntity.addAttribute("identifier").setNillable(false);

		DefaultEntityMetaData refEntity2 = new DefaultEntityMetaData("IntTarget2");
		refEntity2.setIdAttribute("identifier");
		refEntity2.addAttribute("identifier").setDataType(MolgenisFieldTypes.INT).setNillable(false);

		DefaultEntityMetaData varcharMD = new DefaultEntityMetaData("MrefTest").setLabel("ref Test");
		varcharMD.setIdAttribute("identifier");
		varcharMD.addAttribute("identifier").setNillable(false);
		varcharMD.addAttribute("stringRef").setDataType(MolgenisFieldTypes.MREF).setRefEntity(refEntity)
				.setNillable(false);
		varcharMD.addAttribute("intRef").setDataType(MolgenisFieldTypes.MREF).setRefEntity(refEntity2);
		return varcharMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `MrefTest`(`identifier` VARCHAR(255) NOT NULL, PRIMARY KEY (`identifier`)) ENGINE=InnoDB;";
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
		coll.drop(getMetaData().getName());
		coll.drop(getMetaData().getAttribute("stringRef").getRefEntity().getName());
		coll.drop(getMetaData().getAttribute("intRef").getRefEntity().getName());

		// create
		MysqlRepository stringRepo = coll.add(getMetaData().getAttribute("stringRef").getRefEntity());
		MysqlRepository intRepo = coll.add(getMetaData().getAttribute("intRef").getRefEntity());
		MysqlRepository mrefRepo = coll.add(getMetaData());

		Assert.assertEquals(stringRepo.count(), 0);
		Assert.assertEquals(intRepo.count(), 0);
		Assert.assertEquals(mrefRepo.count(), 0);

		Assert.assertEquals(mrefRepo.getCreateSql(), createSql());

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
		entity.set("stringRef", Arrays.asList(new String[]
		{ "ref1", "ref2" }));
		entity.set("intRef", Arrays.asList(new Integer[]
		{ 1, 2 }));
		logger.debug("mref: " + entity);
		mrefRepo.add(entity);

		entity.set("identifier", "two");
		entity.set("stringRef", "ref3");
		entity.set("intRef", null);
		logger.debug("mref: " + entity);
		mrefRepo.add(entity);

		Assert.assertEquals(mrefRepo.count(), 2);

		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", "password", "ROLE_ENTITY_READ_STRINGTARGET2"));
		Assert.assertEquals(
				mrefRepo.getSelectSql(new QueryImpl(), Lists.newArrayList()),
				"SELECT this.`identifier`, GROUP_CONCAT(DISTINCT(`stringRef`.`stringRef`)) AS `stringRef`, "
						+ "GROUP_CONCAT(DISTINCT(`intRef`.`intRef`)) AS `intRef` FROM `MrefTest` AS this "
						+ "LEFT JOIN `MrefTest_stringRef` AS `stringRef_filter` ON (this.`identifier` = `stringRef_filter`.`identifier`) "
						+ "LEFT JOIN `MrefTest_stringRef` AS `stringRef` ON (this.`identifier` = `stringRef`.`identifier`) "
						+ "LEFT JOIN `StringTarget2` AS `StringTarget2_RefTable` ON (`stringRef`.`stringRef` = `StringTarget2_RefTable`.`identifier`) "
						+ "LEFT JOIN `MrefTest_intRef` AS `intRef_filter` ON (this.`identifier` = `intRef_filter`.`identifier`) "
						+ "LEFT JOIN `MrefTest_intRef` AS `intRef` ON (this.`identifier` = `intRef`.`identifier`) "
						+ "GROUP BY this.`identifier`");
		for (Entity e : mrefRepo.findAll(new QueryImpl().eq("identifier", "one")))
		{
			logger.info("found: " + e);
			Assert.assertEquals(e.getList("stringRef"), Arrays.asList(new String[]
			{ "ref1", "ref2" }));
			Assert.assertEquals(e.getList("intRef"), Arrays.asList(new Integer[]
			{ 1, 2 }));

			List<Entity> result = new ArrayList<Entity>();
			for (Entity e2 : e.getEntities("stringRef"))
			{
				result.add(e2);
			}
			Assert.assertEquals(result.get(0).getString("identifier"), "ref1");
			Assert.assertEquals(result.get(1).getString("identifier"), "ref2");
		}

		for (Entity e : mrefRepo.findAll(new QueryImpl().eq("stringRef", "ref3")))
		{
			logger.debug("found: " + e);
			Assert.assertEquals(e.get("stringRef"), Arrays.asList(new String[]
			{ "ref3" }));
		}

		for (Entity e : mrefRepo.findAll(new QueryImpl().eq("stringRef", "ref1")))
		{
			logger.debug("found: " + e);
			Object obj = e.get("stringRef");
			assertTrue(obj instanceof List<?>);
			Assert.assertEquals(Sets.newHashSet((List<?>) obj), Sets.newHashSet(new String[]
			{ "ref1", "ref2" }));
		}

		for (Entity e : mrefRepo.findAll(new QueryImpl().gt("intRef", 1)))
		{
			logger.debug("found: " + e);
			Assert.assertEquals(e.get("intRef"), Arrays.asList(new Integer[]
			{ 1, 2 }));
		}

		// update

		Entity e = mrefRepo.findOne("one");
		e.set("stringRef", "ref2,ref3");
		mrefRepo.update(e);

		e = mrefRepo.findOne("one");
		Assert.assertEquals(e.getList("stringRef").size(), 2);
		Assert.assertTrue(e.getList("stringRef").contains("ref2"));
		Assert.assertTrue(e.getList("stringRef").contains("ref3"));

		// verify not null error

		// verify default
	}

	@Test
	public void testPerformance()
	{
		final int SIZE = 10;

		List<Entity> eList = new ArrayList<Entity>();
		for (int i = 0; i < SIZE; i++)
		{
			Entity entity = new MapEntity();
			entity.set("identifier", "id" + i);
			entity.set("stringRef", Arrays.asList(new String[]
			{ "ref1", "ref2" }));
			entity.set("intRef", Arrays.asList(new Integer[]
			{ 1, 2 }));
			eList.add(entity);
		}

		MysqlRepository mrefRepo = (MysqlRepository) coll.getRepositoryByEntityName(this.getMetaData().getName());
		long startTime = System.currentTimeMillis();
		mrefRepo.add(eList);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		logger.debug("inserted mrefs with " + SIZE * 1000 / elapsedTime + " records per second");

	}
}
