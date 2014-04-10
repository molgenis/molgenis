package org.molgenis.data.mysql;

import java.beans.PropertyVetoException;
import java.util.Iterator;

import javax.sql.DataSource;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class MysqlBasicTest
{
	DataSource ds;
	Logger logger;

	@BeforeClass
	public void setup() throws PropertyVetoException
	{
		BasicConfigurator.configure();
		logger = Logger.getRootLogger();

		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass("com.mysql.jdbc.Driver");
		dataSource.setJdbcUrl(MysqlTestConstants.URL);
		dataSource.setUser("molgenis");
		dataSource.setPassword("molgenis");

		ds = dataSource;

	}

	@Test
	public void testSql() throws Exception
	{
		// create table person(id int auto_increment primary key, firstName varchar(255), lastName varchar(255));

		DefaultEntityMetaData metaData = new DefaultEntityMetaData("PERSON");
		MysqlRepository repo = new MysqlRepository(ds, metaData);
		repo.drop();

		metaData.addAttribute("firstName").setNillable(false);

        //check default id
        Assert.assertEquals(metaData.getIdAttribute().getName(),"firstName");

        Assert.assertEquals(repo.iteratorSql(), "SELECT firstName FROM PERSON");
		Assert.assertEquals(repo.getInsertSql(), "INSERT INTO PERSON (firstName) VALUES (?)");
		Assert.assertEquals(
				repo.getCreateSql(),
				"CREATE TABLE IF NOT EXISTS PERSON(firstName VARCHAR(255) NOT NULL, PRIMARY KEY (firstName)) ENGINE=InnoDB;");

		metaData.addAttribute("lastName").setNillable(false);


		//check manually set id
		metaData.setIdAttribute("lastName");
        Assert.assertEquals(metaData.getIdAttribute().getName(),"lastName");

        Assert.assertEquals(repo.iteratorSql(), "SELECT firstName, lastName FROM PERSON");
		Assert.assertEquals(repo.getInsertSql(), "INSERT INTO PERSON (firstName, lastName) VALUES (?, ?)");
		Assert.assertEquals(
				repo.getCreateSql(),
				"CREATE TABLE IF NOT EXISTS PERSON(firstName VARCHAR(255) NOT NULL, lastName VARCHAR(255) NOT NULL, PRIMARY KEY (lastName)) ENGINE=InnoDB;");

		metaData.addAttributeMetaData(new DefaultAttributeMetaData("age", MolgenisFieldTypes.FieldTypeEnum.INT));
		Assert.assertEquals(repo.iteratorSql(), "SELECT firstName, lastName, age FROM PERSON");
		Assert.assertEquals(repo.getInsertSql(), "INSERT INTO PERSON (firstName, lastName, age) VALUES (?, ?, ?)");
		Assert.assertEquals(
				repo.getCreateSql(),
				"CREATE TABLE IF NOT EXISTS PERSON(firstName VARCHAR(255) NOT NULL, lastName VARCHAR(255) NOT NULL, age INTEGER, PRIMARY KEY (lastName)) ENGINE=InnoDB;");
		Assert.assertEquals(repo.getCountSql(new QueryImpl()), "SELECT count(this.lastName) FROM PERSON AS this");

		// where
		Assert.assertEquals(repo.getWhereSql(new QueryImpl().eq("firstName", "John")), "this.firstName = 'John'");
		Assert.assertEquals(repo.getWhereSql(new QueryImpl().eq("firstName", "John").eq("age", "5")),
				"this.firstName = 'John' AND this.age = 5");

		repo.create();

		// Entity generator to monitor performance
		final int SIZE = 100;
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
