package org.molgenis.data.mysql;

import java.beans.PropertyVetoException;
import java.util.Locale;

import javax.sql.DataSource;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.molgenis.AppConfig;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@ContextConfiguration(classes = AppConfig.class)
public class MysqlRepositoryCollectionTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	MysqlRepositoryCollection coll;
	DataSource ds;
	Logger logger;

	@Test
	public void test()
	{
		// delete old stuff
		coll.drop("coll_person");

		// create collection, add repo, destroy and reload
		DefaultEntityMetaData personMD = new DefaultEntityMetaData("coll_person");
		personMD.setIdAttribute("email");
		personMD.addAttribute("email").setNillable(false);
		personMD.addAttribute("firstName");
		personMD.addAttribute("lastName");
		personMD.addAttribute("birthday").setDataType(MolgenisFieldTypes.DATE);
		personMD.addAttribute("height").setDataType(MolgenisFieldTypes.INT);
		personMD.addAttribute("active").setDataType(MolgenisFieldTypes.BOOL);

		// autowired ds
		coll.add(personMD);

		// destroy and rebuild
		Assert.assertNotNull(coll.getRepositoryByEntityName("coll_person"));

		MysqlRepository repo = (MysqlRepository) coll.getRepositoryByEntityName("coll_person");
		String[] locale = Locale.getISOCountries();
		for (int i = 0; i < 10; i++)
		{
			Entity e = new MapEntity();
			e.set("email", i + "@localhost");
			e.set("firstName", locale[i]);
			e.set("height", 170 + i);
			e.set("birthday", "1992-03-1" + i);
			e.set("active", i % 2 == 0);
			repo.add(e);
		}

		// and again
		repo = (MysqlRepository) coll.getRepositoryByEntityName("coll_person");
		Assert.assertEquals(repo.count(), 10);
	}

	@BeforeClass
	public void setup() throws PropertyVetoException
	{
		BasicConfigurator.configure();
		logger = Logger.getRootLogger();

		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setDriverClass("com.mysql.jdbc.Driver");
		dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/omx?rewriteBatchedStatements=true)");
		dataSource.setUser("molgenis");
		dataSource.setPassword("molgenis");

		ds = dataSource;
	}
}
