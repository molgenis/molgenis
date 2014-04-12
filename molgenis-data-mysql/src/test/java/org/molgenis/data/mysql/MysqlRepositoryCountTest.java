package org.molgenis.data.mysql;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/** Test for Query */
public class MysqlRepositoryCountTest
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
		dataSource.setJdbcUrl(MysqlRepositoryTestConstants.URL);
		dataSource.setUser("molgenis");
		dataSource.setPassword("molgenis");

		ds = dataSource;
	}

	@Test
	public void test()
	{
		// define model
		DefaultEntityMetaData countryMD = new DefaultEntityMetaData("query_country");
		countryMD.addAttribute("code").setNillable(false); // TODO: make this an enum!

		DefaultEntityMetaData personMD = new DefaultEntityMetaData("query_person");
		personMD.addAttribute("email").setNillable(false);
		personMD.addAttribute("firstName");
		personMD.addAttribute("lastName");
		personMD.addAttribute("birthday").setDataType(MolgenisFieldTypes.DATE);
		personMD.addAttribute("height").setDataType(MolgenisFieldTypes.INT);
		personMD.addAttribute("active").setDataType(MolgenisFieldTypes.BOOL);
		personMD.addAttribute("country").setDataType(MolgenisFieldTypes.XREF).setRefEntity(countryMD);

		MysqlRepository countries = new MysqlRepository(ds, countryMD);
		MysqlRepository persons = new MysqlRepository(ds, personMD);

		// drop and create the tables via the repository
		persons.drop();
		countries.drop();
		countries.create();
		persons.create();

		// add country entities to repo
		Entity c = new MapEntity();
		c.set("code", "US");
		countries.add(c);
		c.set("code", "NL");
		countries.add(c);

		Entity e = new MapEntity();
		e.set("email", "foo@localhost");
		e.set("firstName", "john");
		e.set("lastName", "doe");
		e.set("birthday", "1976-06-07");
		e.set("height", 180);
		e.set("active", true);
		e.set("country", "US");
		persons.add(e);

		// add person entities to repo
		e.set("email", "bar@localhost");
		e.set("firstName", "jane");
		e.set("lastName", "doe");
		e.set("birthday", "1980-06-07");
		e.set("height", 165);
		e.set("active", false);
		e.set("country", "US");
		persons.add(e);

		e.set("email", "donald@localhost");
		e.set("firstName", "donald");
		e.set("lastName", "duck");
		e.set("birthday", "1950-01-31");
		e.set("height", 55);
		e.set("active", true);
		e.set("country", "NL");
		persons.add(e);

		// query test

		Assert.assertEquals(persons.count(), 3);
		// string
		Assert.assertEquals(persons.count(new QueryImpl().eq("lastName", "doe")), 2);
		Assert.assertEquals(persons.count(new QueryImpl().eq("lastName", "duck")), 1);
		Assert.assertEquals(persons.count(new QueryImpl().eq("lastName", "duck").or().eq("lastName", "doe")), 3);

		// int
		Assert.assertEquals(persons.count(new QueryImpl().eq("height", 180)), 1);
		Assert.assertEquals(persons.count(new QueryImpl().lt("height", 180)), 2);
		Assert.assertEquals(persons.count(new QueryImpl().le("height", 180)), 3);
		Assert.assertEquals(persons.count(new QueryImpl().lt("height", 180).gt("height", 55)), 1);
		Assert.assertEquals(persons.count(new QueryImpl().gt("height", 165).or().lt("height", 165)), 2);

		// bool
		logger.debug(persons.getSelectSql(new QueryImpl().eq("active", true)));
		Assert.assertEquals(persons.count(new QueryImpl().eq("active", true)), 2);
		Assert.assertEquals(persons.count(new QueryImpl().eq("active", false)), 1);
		Assert.assertEquals(persons.count(new QueryImpl().eq("active", true).or().eq("height", 165)), 3);

		// date
		Assert.assertEquals(persons.count(new QueryImpl().eq("birthday", "1950-01-31")), 1);
		Assert.assertEquals(persons.count(new QueryImpl().gt("birthday", "1950-01-31")), 2);
		Assert.assertEquals(
				persons.count(new QueryImpl().gt("birthday", "1976-06-07").or().lt("birthday", "1976-06-07")), 2);

		// xref
		Assert.assertEquals(persons.count(new QueryImpl().eq("country", "US")), 2);
		Assert.assertEquals(persons.count(new QueryImpl().eq("country", "NL")), 1);
		Assert.assertEquals(persons.count(new QueryImpl().eq("country", "US").gt("height", 165)), 1);
	}
}
