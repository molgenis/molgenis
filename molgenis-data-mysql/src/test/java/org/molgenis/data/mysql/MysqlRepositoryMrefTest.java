package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
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
		refEntity.addAttribute("identifier").setNillable(false).setIdAttribute(true);
		refEntity.addAttribute("label");

		DefaultEntityMetaData refEntity2 = new DefaultEntityMetaData("IntTarget2");
		refEntity2.addAttribute("identifier").setDataType(MolgenisFieldTypes.INT).setNillable(false)
				.setIdAttribute(true);

		EditableEntityMetaData varcharMD = new DefaultEntityMetaData("MrefTest").setLabel("ref Test");
		varcharMD.addAttribute("identifier").setNillable(false).setIdAttribute(true);
		varcharMD.addAttribute("stringRef").setDataType(MolgenisFieldTypes.MREF).setRefEntity(refEntity)
				.setNillable(false);
		varcharMD.addAttribute("intRef").setDataType(MolgenisFieldTypes.MREF).setRefEntity(refEntity2)
				.setNillable(true);
		return varcharMD;
	}

	@Override
	public String createSql()
	{
		return "CREATE TABLE IF NOT EXISTS `MrefTest`(`identifier` VARCHAR(255) NOT NULL, PRIMARY KEY (`identifier`)) ENGINE=InnoDB;";
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
		metaDataService.deleteEntityMeta(getMetaData().getName());
		metaDataService.deleteEntityMeta(getMetaData().getAttribute("stringRef").getRefEntity().getName());
		metaDataService.deleteEntityMeta(getMetaData().getAttribute("intRef").getRefEntity().getName());

		// create
		Repository stringRepo = metaDataService.addEntityMeta(getMetaData().getAttribute("stringRef").getRefEntity());
		Repository intRepo = metaDataService.addEntityMeta(getMetaData().getAttribute("intRef").getRefEntity());
		MysqlRepository mrefRepo = (MysqlRepository) metaDataService.addEntityMeta(getMetaData());

		Assert.assertEquals(stringRepo.count(), 0);
		Assert.assertEquals(intRepo.count(), 0);
		Assert.assertEquals(mrefRepo.count(), 0);

		Assert.assertEquals(mrefRepo.getCreateSql(), createSql());

		// add records
		Entity entity = new MapEntity("identifier");
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

		Entity ref1 = new MapEntity("identifier");
		ref1.set("identifier", "ref1");

		Entity ref2 = new MapEntity("identifier");
		ref2.set("identifier", "ref2");

		Entity ref3 = new MapEntity("identifier");
		ref3.set("identifier", "ref3");

		entity.set("stringRef", Arrays.asList(ref1, ref2));

		Entity intRef1 = new MapEntity("identifier");
		intRef1.set("identifier", 1);

		Entity intRef2 = new MapEntity("identifier");
		intRef2.set("identifier", 2);

		entity.set("intRef", Arrays.asList(intRef1, intRef2));

		LOG.debug("mref: " + entity);
		mrefRepo.add(entity);

		entity.set("identifier", "two");
		entity.set("stringRef", Arrays.asList(ref3));
		entity.set("intRef", null);
		LOG.debug("mref: " + entity);
		mrefRepo.add(entity);

		Assert.assertEquals(mrefRepo.count(), 2);

		Assert.assertEquals(mrefRepo.getSelectSql(new QueryImpl(), Lists.newArrayList()), "SELECT this.`identifier`, "
				+ "GROUP_CONCAT(DISTINCT(`stringRef`.`stringRef`) ORDER BY `stringRef`.`order`) AS `stringRef`, "
				+ "GROUP_CONCAT(DISTINCT(`intRef`.`intRef`) ORDER BY `intRef`.`order`) AS `intRef` "
				+ "FROM `MrefTest` AS this "
				+ "LEFT JOIN `MrefTest_stringRef` AS `stringRef` ON (this.`identifier` = `stringRef`.`identifier`) "
				+ "LEFT JOIN `MrefTest_intRef` AS `intRef` ON (this.`identifier` = `intRef`.`identifier`) "
				+ "GROUP BY this.`identifier`");

		assertEquals(mrefRepo.query().eq("identifier", "one").count(), Long.valueOf(1));
		for (Entity e : mrefRepo.findAll(new QueryImpl().eq("identifier", "one")))
		{
			LOG.info("found: " + e);
			Assert.assertEquals(e.getList("stringRef"), Arrays.asList(new String[]
			{ "ref1", "ref2" }));
			Assert.assertEquals(e.getIntList("intRef"), Arrays.asList(new Integer[]
			{ 1, 2 }));

			List<Entity> result = new ArrayList<Entity>();
			for (Entity e2 : e.getEntities("stringRef"))
			{
				result.add(e2);
			}
			Assert.assertEquals(result.get(0).getString("identifier"), "ref1");
			Assert.assertEquals(result.get(1).getString("identifier"), "ref2");
		}

		assertEquals(mrefRepo.query().eq("stringRef", "ref3").count(), Long.valueOf(1));
		for (Entity e : mrefRepo.findAll(new QueryImpl().eq("stringRef", "ref3")))
		{
			LOG.debug("found: " + e);
			Assert.assertEquals(e.getList("stringRef"), Arrays.asList(new String[]
			{ "ref3" }));
		}

		assertEquals(mrefRepo.query().eq("stringRef", "ref1").count(), Long.valueOf(1));
		for (Entity e : mrefRepo.findAll(new QueryImpl().eq("stringRef", "ref1")))
		{
			LOG.debug("found: " + e);
			Iterator<Entity> it = e.getEntities("stringRef").iterator();
			Assert.assertEquals(Sets.newHashSet(it.next().get("identifier"), it.next().get("identifier")),
					Sets.newHashSet(new String[]
					{ "ref1", "ref2" }));
		}

		assertEquals(mrefRepo.query().gt("intRef", 1).count(), Long.valueOf(1));
		for (Entity e : mrefRepo.findAll(new QueryImpl().gt("intRef", 1)))
		{
			LOG.debug("found: " + e);
			Assert.assertEquals(e.getIntList("intRef"), Arrays.asList(new Integer[]
			{ 1, 2 }));
		}

		assertEquals(mrefRepo.query().eq("stringRef", "ref1").and().eq("stringRef", "ref2").count(), Long.valueOf(1));
		assertEquals(mrefRepo.query().eq("intRef", 1).and().eq("intRef", 2).count(), Long.valueOf(1));
		assertEquals(mrefRepo.query().in("stringRef", Arrays.asList("ref1", "ref2")).count(), Long.valueOf(1));
		assertEquals(mrefRepo.query().in("intRef", Arrays.asList(1, 2)).count(), Long.valueOf(1));

		// update
		ref2 = new MapEntity("identifier");
		ref2.set("identifier", "ref2");

		ref3 = new MapEntity("identifier");
		ref3.set("identifier", "ref3");

		Entity e = mrefRepo.findOne("one");
		e.set("stringRef", Arrays.asList(ref2, ref3));
		mrefRepo.update(e);

		e = mrefRepo.findOne("one");
		Assert.assertEquals(Iterables.size(e.getEntities("stringRef")), 2);

		// verify not null error

		// verify default
	}

	@Override
	public void verifyTestEntity(Entity e) throws Exception
	{
		// TODO Auto-generated method stub

	}
}
