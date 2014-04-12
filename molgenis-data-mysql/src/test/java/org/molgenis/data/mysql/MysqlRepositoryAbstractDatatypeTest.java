package org.molgenis.data.mysql;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/** Superclass for all datatype specific tests, e.g. MysqlRepositoryStringTest */
public abstract class MysqlRepositoryAbstractDatatypeTest
{
	DataSource ds;
	Logger logger;
	private EntityMetaData metaData;

	/** Define a data model to test */
	public abstract EntityMetaData createMetaData();

	/** Define the expected mysql create table for the data model */
	public abstract String createSql();

	/** Define a test object to be used */
	public abstract Entity defaultEntity();

	public EntityMetaData getMetaData()
	{
		if (metaData == null)
		{
			metaData = createMetaData();
		}
		return metaData;
	}

	@Test
	public void test() throws Exception
	{
		// test create table
		MysqlRepository repo = new MysqlRepository(ds, getMetaData());
		Assert.assertEquals(repo.getCreateSql(), createSql());
		repo.drop();
		repo.create();

		// verify default value
		Entity defaultEntity = defaultEntity();
		logger.debug("inserting: " + defaultEntity);
		repo.add(defaultEntity());
		for (Entity e : repo)
		{
			logger.debug("found back " + e);
			// Assert.assertEquals(e.get("col3").getClass(),
			// e.get("col3", getMetaData().getAttribute("col3").getDataType().getJavaType());
			Object value = e.get("col3");
			// if(value instanceof BigDecimal)
			// value = Double.valueOf(e.getString("col3"));
			Object defaultValue = repo.getEntityMetaData().getAttribute("col3").getDefaultValue();
			logger.debug("defaultClass=" + defaultValue.getClass().getName() + " - valueClass="
					+ value.getClass().getName());
			Assert.assertEquals(defaultValue, value);

		}

		// verify not null error
		// TODO

		// verify default
		// TODO

		// allow time for logger to finish... (premature end of program results in loss of output)
		Thread.sleep(100);
	}

	/** Setup a datasource to use for testing */
	// TODO use spring injection
	@BeforeClass
	public void setup() throws PropertyVetoException
	{
		BasicConfigurator.configure();
		logger = Logger.getRootLogger();

		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass("com.mysql.jdbc.Driver");
		dataSource.setJdbcUrl(MysqlRepositoryTestConstants.URL);
		dataSource.setUser("molgenis");
		dataSource.setPassword("molgenis");

		ds = dataSource;
	}
}
