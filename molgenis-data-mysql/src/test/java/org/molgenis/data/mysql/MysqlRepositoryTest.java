package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.molgenis.AppConfig;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
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

	@Test
	public void testAggregates()
	{
		coll.dropEntityMetaData("fruit");

		DefaultEntityMetaData meta = new DefaultEntityMetaData("Fruit");
		meta.addAttribute("name").setIdAttribute(true).setNillable(false);
		meta.addAttribute("type").setAggregateable(true);
		MysqlRepository repo = coll.add(meta);

		Entity elstar = new MapEntity("name", "Elstar");
		elstar.set("type", "Apple");
		repo.add(elstar);

		Entity jonagold = new MapEntity("name", "Jonagold");
		jonagold.set("type", "Apple");
		repo.add(jonagold);

		Entity conference = new MapEntity("name", "Conference");
		conference.set("type", "Pear");
		repo.add(conference);

		AggregateResult result = repo.aggregate(meta.getAttribute("type"), null, new QueryImpl());
		Assert.assertEquals(result.getxLabels(), Arrays.asList("Pear", "Apple", "Total"));
		Assert.assertEquals(result.getyLabels(), Arrays.asList("Count"));
		Assert.assertEquals(result.getMatrix(), Arrays.asList(Arrays.asList(1l), Arrays.asList(2l), Arrays.asList(3l)));
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

		coll.dropEntityMetaData(metaData.getName());
		MysqlRepository repo = coll.add(metaData);

		Assert.assertEquals(repo.iteratorSql(), "SELECT firstName, lastName FROM MysqlPerson");
		Assert.assertEquals(repo.getInsertSql(), "INSERT INTO `MysqlPerson` (`firstName`, `lastName`) VALUES (?, ?)");
		Assert.assertEquals(
				repo.getCreateSql(),
				"CREATE TABLE IF NOT EXISTS `MysqlPerson`(`firstName` VARCHAR(255) NOT NULL, `lastName` VARCHAR(255) NOT NULL, PRIMARY KEY (`lastName`)) ENGINE=InnoDB;");

		metaData.addAttributeMetaData(new DefaultAttributeMetaData("age", MolgenisFieldTypes.FieldTypeEnum.INT));
		Assert.assertEquals(repo.iteratorSql(), "SELECT firstName, lastName, age FROM MysqlPerson");
		Assert.assertEquals(repo.getInsertSql(),
				"INSERT INTO `MysqlPerson` (`firstName`, `lastName`, `age`) VALUES (?, ?, ?)");
		Assert.assertEquals(
				repo.getCreateSql(),
				"CREATE TABLE IF NOT EXISTS `MysqlPerson`(`firstName` VARCHAR(255) NOT NULL, `lastName` VARCHAR(255) NOT NULL, `age` INTEGER, PRIMARY KEY (`lastName`)) ENGINE=InnoDB;");
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

		coll.dropEntityMetaData(metaData.getName());
		repo = coll.add(metaData);

		// Entity generator to monitor performance (set batch to 100000 to show up to >10,000 records/second)
		final int SIZE = 100000;
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

					@Override
					public void remove()
					{

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

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("test");
		entityMetaData.setIdAttribute(idAttributeName);
		entityMetaData.setLabelAttribute(idAttributeName);
		DefaultAttributeMetaData idAttributeMetaData = new DefaultAttributeMetaData(idAttributeName);
		idAttributeMetaData.setDataType(MolgenisFieldTypes.STRING);
		idAttributeMetaData.setIdAttribute(true);
		idAttributeMetaData.setLabelAttribute(true);
		idAttributeMetaData.setNillable(false);
		entityMetaData.addAttributeMetaData(idAttributeMetaData);

		MysqlRepository testRepository = coll.add(entityMetaData);

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
