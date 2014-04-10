package org.molgenis.data.mysql;

import java.beans.PropertyVetoException;
import java.math.BigDecimal;

import javax.sql.DataSource;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public abstract class AbstractMysqlDatatypeTest
{
	DataSource ds;
	Logger logger;
	private EntityMetaData metaData;

	public abstract EntityMetaData createMetaData();

	public abstract String createSql();

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
		// create
		MysqlRepository repo = new MysqlRepository(ds, getMetaData());
		Assert.assertEquals(repo.getCreateSql(), createSql());
		repo.drop();
		repo.create();

		// verify default value
        Entity defaultEntity = defaultEntity();
        logger.debug("inserting: "+defaultEntity);
		repo.add(defaultEntity());
		for (Entity e : repo)
		{
			logger.debug("found back "+e);
			// Assert.assertEquals(e.get("col3").getClass(),
			//e.get("col3", getMetaData().getAttribute("col3").getDataType().getJavaType());
            Object value =  e.get("col3");
            //if(value instanceof BigDecimal)
            //    value = Double.valueOf(e.getString("col3"));
            Object defaultValue = repo.getEntityMetaData().getAttribute("col3").getDefaultValue();
            logger.debug("defaultClass="+defaultValue.getClass().getName()+" - valueClass="+value.getClass().getName());
			Assert.assertEquals(defaultValue,value);

		}

		// verify not null error

		// verify default

        //time for logger to finish...
        Thread.sleep(100);
	}

	@Test
	public void testXref()
	{
        //create SQL should join in the identifier field

	}

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
}
