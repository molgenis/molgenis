package org.molgenis.data.mysql;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.molgenis.AppConfig;
import org.molgenis.MolgenisFieldTypes;
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

/** Simple test of all apsects of the repository */
@ContextConfiguration(classes = AppConfig.class)
public class MysqlRepositoryTest  extends AbstractTestNGSpringContextTests
{
    @Autowired
    MysqlRepositoryCollection coll;
	Logger logger = Logger.getLogger(getClass());

	@Test
	public void testSql() throws Exception
	{
		// create table person(id int auto_increment primary key, firstName varchar(255), lastName varchar(255));

		DefaultEntityMetaData metaData = new DefaultEntityMetaData("MysqlPerson");

		metaData.addAttribute("firstName").setNillable(false);

		// check default id (first attribute is used if not entity.setIdAttribute)

		// Assert.assertEquals(metaData.getIdAttribute().getName(), "firstName");

		// Assert.assertEquals(repo.iteratorSql(), "SELECT firstName FROM PERSON");
		// Assert.assertEquals(repo.getInsertSql(), "INSERT INTO PERSON (firstName) VALUES (?)");
		// Assert.assertEquals(repo.getCreateSql(),
		// "CREATE TABLE IF NOT EXISTS PERSON(firstName VARCHAR(255) NOT NULL, PRIMARY KEY (firstName)) ENGINE=InnoDB;");

		metaData.addAttribute("lastName").setNillable(false);

		// check manually set id (using setIdAttribute)

		metaData.setIdAttribute("lastName");
		Assert.assertEquals(metaData.getIdAttribute().getName(), "lastName");

        coll.drop(metaData.getName());
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
		Assert.assertEquals(repo.getCountSql(new QueryImpl()),
				"SELECT COUNT(DISTINCT this.`lastName`) FROM `MysqlPerson` AS this");

		// test where clauses
		Assert.assertEquals(repo.getWhereSql(new QueryImpl().eq("firstName", "John")),
				"WHERE this.`firstName` = 'John'");
		Assert.assertEquals(repo.getWhereSql(new QueryImpl().eq("firstName", "John").eq("age", "5")),
				"WHERE this.`firstName` = 'John' AND this.`age` = 5");

        //search
        Assert.assertEquals(repo.getWhereSql(new QueryImpl().search("John")),
				"WHERE (this.`firstName` LIKE '%John%' OR this.`lastName` LIKE '%John%' OR CAST(this.`age` as CHAR) LIKE '%John%')");

        //sort
        Assert.assertEquals(repo.getSortSql(new QueryImpl().sort(Sort.Direction.ASC,"firstName"))
,
				"ORDER BY `firstName` ASC");
        Assert.assertEquals(repo.getSortSql(new QueryImpl().sort(Sort.Direction.DESC,"firstName"))
,
				"ORDER BY `firstName` DESC");
        Assert.assertEquals(repo.getWhereSql(new QueryImpl().eq("firstName", "John").sort(Sort.Direction.DESC,"firstName"))
,
				"WHERE this.`firstName` = 'John'");

        //test delete clauses
		Assert.assertEquals(repo.getDeleteSql(), "DELETE FROM `MysqlPerson` WHERE `lastName` = ?");

        coll.drop(metaData.getName());
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
        for(Entity e: repo.findAll(new QueryImpl().lt("age", 5)))
        {
            repo.delete(e);
            break;
        }
        Assert.assertEquals(3, count(repo, new QueryImpl().lt("age", 5)));

        //update one
        Entity e = repo.findOne("Doe2");
        e.set("firstName","Updated");
        repo.update(e);

        //find change
        e = repo.findOne("Doe2");
        Assert.assertEquals(e.getString("firstName"),"Updated");
    }

	public int count(MysqlRepository repo, Query query)
	{
		int count = 0;
		for (Entity e : repo.findAll(query))
		{
			count++;
		}
		return count;
	}
}
