package org.molgenis.data.mysql;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MysqlTestConfig;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/** Simple test of all apsects of the repository */
@ContextConfiguration(classes = MysqlTestConfig.class)
public class MysqlRepositoryTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	MysqlRepositoryCollection coll;

	@Autowired
	MetaDataServiceImpl metaDataRepositories;

	@Test
	public void addStreamFindAll()
	{
		DefaultEntityMetaData metaData = new DefaultEntityMetaData("IntValue");
		metaData.addAttribute("intAttr").setDataType(MolgenisFieldTypes.INT).setIdAttribute(true).setNillable(false);

		Repository repo = metaDataRepositories.addEntityMeta(metaData);

		int count = 2099;
		List<Entity> entities = new ArrayList<>();
		for (int i = 0; i < count; i++)
		{
			Entity e = new MapEntity("intAttr");
			e.set("intAttr", i);
			entities.add(e);
		}
		repo.add(entities.stream());

		int i = 0;
		for (Entity e : repo)
		{
			assertEquals(e.getInt("intAttr"), Integer.valueOf(i++));
		}

		assertEquals(Iterables.size(repo), count);
		assertEquals(repo.findAll(new QueryImpl().ge("intAttr", 999)).collect(toList()).size(), 1100);
		assertEquals(repo.findAll(new QueryImpl().eq("intAttr", 999)).collect(toList()).size(), 1);
		assertEquals(repo.findAll(new QueryImpl().eq("intAttr", -1)).collect(toList()).size(), 0);
		assertEquals(repo.findAll(new QueryImpl().le("intAttr", count)).collect(toList()).size(), count);

		List<Entity> pagedEntities = repo.findAll(new QueryImpl().setOffset(10).setPageSize(20)).collect(toList());
		assertEquals(pagedEntities.size(), 20);
		i = 10;
		for (Entity e : pagedEntities)
		{
			assertEquals(e.getInt("intAttr"), Integer.valueOf(i++));
		}

		repo.deleteAll(); // cleanup
	}

	@Test
	public void addStreamUpdateStreamFindAll()
	{
		DefaultEntityMetaData metaData = new DefaultEntityMetaData("addStreamUpdateStreamFindAll");
		metaData.addAttribute("intAttr").setDataType(MolgenisFieldTypes.INT).setIdAttribute(true).setNillable(false);
		metaData.addAttribute("strAttr").setNillable(false);

		Repository repo = metaDataRepositories.addEntityMeta(metaData);

		int count = 2099;
		List<Entity> entities = new ArrayList<>();
		for (int i = 0; i < count; i++)
		{
			Entity e = new MapEntity("intAttr");
			e.set("intAttr", i);
			e.set("strAttr", "str" + i);
			entities.add(e);
		}
		repo.add(entities.stream());

		Entity entity0 = entities.get(0);
		entity0.set("strAttr", "newstr0");
		Entity entity1 = entities.get(1);
		entity1.set("strAttr", "newstr1");
		repo.update(Stream.of(entity0, entity1));
		assertNull(repo.findOne(new QueryImpl().eq("strAttr", "str0")));
		assertNull(repo.findOne(new QueryImpl().eq("strAttr", "str1")));
		assertNotNull(repo.findOne(new QueryImpl().eq("strAttr", "newstr0")));
		assertNotNull(repo.findOne(new QueryImpl().eq("strAttr", "newstr1")));
		repo.deleteAll(); // cleanup
	}

	@Test
	public void addStreamDeleteStreamFindAll()
	{
		DefaultEntityMetaData metaData = new DefaultEntityMetaData("IntValue");
		metaData.addAttribute("intAttr").setDataType(MolgenisFieldTypes.INT).setIdAttribute(true).setNillable(false);

		Repository repo = metaDataRepositories.addEntityMeta(metaData);

		int count = 2099;
		List<Entity> entities = new ArrayList<>();
		List<Entity> entitiesToDelete = new ArrayList<>();
		for (int i = 0; i < count; i++)
		{
			Entity e = new MapEntity("intAttr");
			e.set("intAttr", i);
			entities.add(e);
			if (i < 100)
			{
				entitiesToDelete.add(e);
			}
		}
		repo.add(entities.stream());
		repo.delete(entitiesToDelete.stream());

		int i = 100;
		for (Entity e : repo)
		{
			assertEquals(e.getInt("intAttr"), Integer.valueOf(i++));
		}

		assertEquals(Iterables.size(repo), count - 100);
		assertEquals(repo.findAll(new QueryImpl().ge("intAttr", 999)).collect(toList()).size(), 1100);
		assertEquals(repo.findAll(new QueryImpl().eq("intAttr", 999)).collect(toList()).size(), 1);
		assertEquals(repo.findAll(new QueryImpl().eq("intAttr", -1)).collect(toList()).size(), 0);
		assertEquals(repo.findAll(new QueryImpl().le("intAttr", count)).collect(toList()).size(), count - 100);

		List<Entity> pagedEntities = repo.findAll(new QueryImpl().setOffset(10).setPageSize(20)).collect(toList());
		assertEquals(pagedEntities.size(), 20);
		i = 10 + 100;
		for (Entity e : pagedEntities)
		{
			assertEquals(e.getInt("intAttr"), Integer.valueOf(i++));
		}
		repo.deleteAll(); // cleanup
	}

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
		assertEquals(repo.findAll(new QueryImpl().ge("intAttr", 999)).collect(toList()).size(), 1100);
		assertEquals(repo.findAll(new QueryImpl().eq("intAttr", 999)).collect(toList()).size(), 1);
		assertEquals(repo.findAll(new QueryImpl().eq("intAttr", -1)).collect(toList()).size(), 0);
		assertEquals(repo.findAll(new QueryImpl().le("intAttr", count)).collect(toList()).size(), count);

		List<Entity> pagedEntities = repo.findAll(new QueryImpl().setOffset(10).setPageSize(20)).collect(toList());
		assertEquals(pagedEntities.size(), 20);
		i = 10;
		for (Entity e : pagedEntities)
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
		metaData.addAttribute("lastName").setNillable(false).setIdAttribute(true);

		// check manually set id (using setIdAttribute)

		metaData.setIdAttribute("lastName");
		Assert.assertEquals(metaData.getIdAttribute().getName(), "lastName");

		MysqlRepository repo = (MysqlRepository) metaDataRepositories.addEntityMeta(metaData);

		Assert.assertEquals(repo.getInsertSql(), "INSERT INTO `MysqlPerson` (`firstName`, `lastName`) VALUES (?, ?)");
		Assert.assertEquals(repo.getCreateSql(),
				"CREATE TABLE IF NOT EXISTS `MysqlPerson`(`firstName` TEXT NOT NULL, `lastName` VARCHAR(255) NOT NULL, PRIMARY KEY (`lastName`)) ENGINE=InnoDB;");

		metaData.addAttribute("age").setDataType(MolgenisFieldTypes.INT);
		metaDataRepositories.updateEntityMeta(metaData);

		Assert.assertEquals(repo.getInsertSql(),
				"INSERT INTO `MysqlPerson` (`firstName`, `lastName`, `age`) VALUES (?, ?, ?)");
		Assert.assertEquals(repo.getCreateSql(),
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
		Assert.assertEquals(repo.getSortSql(new QueryImpl().sort(new Sort("firstName", Sort.Direction.ASC))),
				"ORDER BY `firstName` ASC");
		Assert.assertEquals(repo.getSortSql(new QueryImpl().sort(new Sort("firstName", Sort.Direction.DESC))),
				"ORDER BY `firstName` DESC");

		params.clear();
		Assert.assertEquals(repo.getWhereSql(
				new QueryImpl().eq("firstName", "John").sort(new Sort("firstName", Sort.Direction.DESC)), params, 0),
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
		repo.add(StreamSupport.stream(iterable.spliterator(), false));
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
		for (Entity e : repo.findAll(new QueryImpl().lt("age", 5)).collect(toList()))
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
		Entity e : repo.findAll(query).collect(toList()))
		{
			count++;
		}
		return count;
	}

	@Test
	public void findAllIterableObject_Iterable()
	{
		String idAttributeName = "id";
		final String exampleId0 = "id123";
		final String exampleId1 = "id456";

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

		MapEntity entity0 = new MapEntity();
		entity0.set(idAttributeName, exampleId0);
		MapEntity entity1 = new MapEntity();
		entity1.set(idAttributeName, exampleId1);
		testRepository.add(entity1); // add in reverse order, so we can check if returned in the correct order
		testRepository.add(entity0);

		List<Entity> entities = testRepository.findAll(Stream.of(exampleId0, "missing", exampleId1, "nope"))
				.collect(toList());

		assertEquals(entities.size(), 2);
		Iterator<Entity> it = entities.iterator();
		assertEquals(it.next().getIdValue(), exampleId0);
		assertEquals(it.next().getIdValue(), exampleId1);

		testRepository.deleteAll(); // cleanup
	}
}
