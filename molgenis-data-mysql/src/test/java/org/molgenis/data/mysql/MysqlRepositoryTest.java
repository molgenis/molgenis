package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.molgenis.AppConfig;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/** Simple test of all apsects of the repository */
@ContextConfiguration(classes = AppConfig.class)
public class MysqlRepositoryTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	MysqlRepositoryCollection coll;

	@Autowired
	MetaDataServiceImpl metaDataRepositories;

	@Test
	public void testFindAll()
	{
		DefaultEntityMetaData metaData = new DefaultEntityMetaData("IntValue");
		metaData.addAttribute("intAttr").setDataType(MolgenisFieldTypes.INT).setIdAttribute(true).setNillable(false);

		Repository repo = metaDataRepositories.addEntityMeta(metaData);

		int count = 2099;
		for (int i = 0; i < count; i++)
		{
			Entity e = new MapEntity("intAttr");
			e.set("intAttr", i);
			repo.add(e);
		}

		int i = 0;
		for (Entity e : repo)
		{
			assertEquals(e.getInt("intAttr"), Integer.valueOf(i++));
		}

		assertEquals(Iterables.size(repo), count);
		assertEquals(Iterables.size(repo.findAll(new QueryImpl().ge("intAttr", 999))), 1100);
		assertEquals(Iterables.size(repo.findAll(new QueryImpl().eq("intAttr", 999))), 1);
		assertEquals(Iterables.size(repo.findAll(new QueryImpl().eq("intAttr", -1))), 0);
		assertEquals(Iterables.size(repo.findAll(new QueryImpl().le("intAttr", count))), count);

		Iterable<Entity> it = repo.findAll(new QueryImpl().setOffset(10).setPageSize(20));
		assertEquals(Iterables.size(it), 20);
		i = 10;
		for (Entity e : it)
		{
			assertEquals(e.getInt("intAttr"), Integer.valueOf(i++));
		}
	}

	@Test
	public void testSql() throws Exception
	{
		// create table person(id int auto_increment primary key, firstName varchar(255), lastName varchar(255));

		DefaultEntityMetaData metaData = new DefaultEntityMetaData("MysqlPerson");

		metaData.addAttribute("firstName").setNillable(false);
		metaData.addAttribute("lastName").setNillable(false);

		// check manually set id (using setIdAttribute)

		metaData.setIdAttribute("lastName");
		Assert.assertEquals(metaData.getIdAttribute().getName(), "lastName");

		MysqlRepository repo = (MysqlRepository) metaDataRepositories.addEntityMeta(metaData);

		Assert.assertEquals(repo.getInsertSql(), "INSERT INTO `MysqlPerson` (`firstName`, `lastName`) VALUES (?, ?)");
		Assert.assertEquals(
				repo.getCreateSql(),
				"CREATE TABLE IF NOT EXISTS `MysqlPerson`(`firstName` TEXT NOT NULL, `lastName` VARCHAR(255) NOT NULL, PRIMARY KEY (`lastName`)) ENGINE=InnoDB;");

		metaData.addAttribute("age").setDataType(MolgenisFieldTypes.INT);
		metaDataRepositories.updateEntityMeta(metaData);

		Assert.assertEquals(repo.getInsertSql(),
				"INSERT INTO `MysqlPerson` (`firstName`, `lastName`, `age`) VALUES (?, ?, ?)");
		Assert.assertEquals(
				repo.getCreateSql(),
				"CREATE TABLE IF NOT EXISTS `MysqlPerson`(`firstName` TEXT NOT NULL, `lastName` VARCHAR(255) NOT NULL, `age` INTEGER, PRIMARY KEY (`lastName`)) ENGINE=InnoDB;");
		Assert.assertEquals(repo.getCountSql(new QueryImpl(), Lists.newArrayList()),
				"SELECT COUNT(DISTINCT this.`lastName`) FROM `MysqlPerson` AS this");

		// test where clauses
		List<Object> params = Lists.newArrayList();
		Assert.assertEquals(repo.getWhereSql(new QueryImpl().eq("firstName", "John"), params, 0),
				"this.`firstName` = ?");
		Assert.assertEquals(params, Lists.<Object> newArrayList("John"));

		params.clear();
		Assert.assertEquals(repo.getWhereSql(new QueryImpl().eq("firstName", "John").eq("age", "5"), params, 0),
				"this.`firstName` = ?  AND this.`age` = ?");
		Assert.assertEquals(params, Lists.<Object> newArrayList("John", 5));

		// search
		params.clear();
		Assert.assertEquals(repo.getWhereSql(new QueryImpl().search("John"), params, 0),
				"(this.`firstName` LIKE ? OR this.`lastName` LIKE ? OR CAST(this.`age` as CHAR) LIKE ?)");
		Assert.assertEquals(params, Lists.<Object> newArrayList("%John%", "%John%", "%John%"));

		// sort
		Assert.assertEquals(repo.getSortSql(new QueryImpl().sort(Sort.Direction.ASC, "firstName")),
				"ORDER BY `firstName` ASC");
		Assert.assertEquals(repo.getSortSql(new QueryImpl().sort(Sort.Direction.DESC, "firstName")),
				"ORDER BY `firstName` DESC");

		params.clear();
		Assert.assertEquals(repo.getWhereSql(
				new QueryImpl().eq("firstName", "John").sort(Sort.Direction.DESC, "firstName"), params, 0),
				"this.`firstName` = ?");
		Assert.assertEquals(params, Lists.<Object> newArrayList("John"));

		// test delete clauses
		Assert.assertEquals(repo.getDeleteSql(), "DELETE FROM `MysqlPerson` WHERE `lastName` = ?");

		coll.deleteEntityMeta(metaData.getName());
		repo = (MysqlRepository) coll.addEntityMeta(metaData);

		// Entity generator to monitor performance (set batch to 100000 to show up to >10,000 records/second)
		final int SIZE = 1000;
		Iterable<Entity> iterable = new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				return new Iterator<Entity>()
				{
					int count = 1;

					@Override
					public boolean hasNext()
					{
						if (count <= SIZE)
						{
							return true;
						}
						else return false;
					}

					@Override
					public Entity next()
					{
						Entity e = new MapEntity();
						e.set("firstName", "John" + count);
						e.set("lastName", "Doe" + count);
						e.set("age", count);
						count++;
						return e;
					}
				};
			}
		};
		long startTime = System.currentTimeMillis();
		repo.add(iterable);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		logger.debug("inserted with " + SIZE * 1000 / elapsedTime + " records per second");

		// count
		Assert.assertEquals(SIZE, repo.count());
		Assert.assertEquals(4, repo.count(new QueryImpl().lt("age", 5)));

		// select
		Assert.assertEquals(4, count(repo, new QueryImpl().lt("age", 5)));
		Assert.assertEquals(SIZE, count(repo, new QueryImpl()));

		// delete one
		for (Entity e : repo.findAll(new QueryImpl().lt("age", 5)))
		{
			repo.delete(e);
			break;
		}
		Assert.assertEquals(3, count(repo, new QueryImpl().lt("age", 5)));

		// update one
		Entity e = repo.findOne("Doe2");
		e.set("firstName", "Updated");
		repo.update(e);

		// find change
		e = repo.findOne("Doe2");
		Assert.assertEquals(e.getString("firstName"), "Updated");
	}

	public int count(MysqlRepository repo, Query query)
	{
		int count = 0;
		for (@SuppressWarnings("unused")
		Entity e : repo.findAll(query))
		{
			count++;
		}
		return count;
	}

	@Test
	public void findAllIterableObject_Iterable()
	{
		String idAttributeName = "id";
		final String exampleId = "id123";

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("testje");

		entityMetaData.setIdAttribute(idAttributeName);
		entityMetaData.setLabelAttribute(idAttributeName);
		DefaultAttributeMetaData idAttributeMetaData = new DefaultAttributeMetaData(idAttributeName);
		idAttributeMetaData.setDataType(MolgenisFieldTypes.STRING);
		idAttributeMetaData.setIdAttribute(true);
		idAttributeMetaData.setLabelAttribute(true);
		idAttributeMetaData.setNillable(false);
		entityMetaData.addAttributeMetaData(idAttributeMetaData);

		Repository testRepository = coll.addEntityMeta(entityMetaData);

		MapEntity entity = new MapEntity();
		entity.set(idAttributeName, exampleId);
		testRepository.add(entity);

		Iterable<Entity> entities = testRepository.findAll(new Iterable<Object>()
		{
			@Override
			public Iterator<Object> iterator()
			{
				return Collections.<Object> singletonList(exampleId).iterator();
			}
		});

		assertEquals(Iterables.size(entities), 1);
		assertEquals(entities.iterator().next().getIdValue(), exampleId);
	}
}
