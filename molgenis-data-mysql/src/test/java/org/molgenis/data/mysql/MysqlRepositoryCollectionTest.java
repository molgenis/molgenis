package org.molgenis.data.mysql;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.molgenis.AppConfig;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCollection;
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
		new MysqlRepository(ds,new DefaultEntityMetaData("attributes")).drop();
		new MysqlRepository(ds,new DefaultEntityMetaData("entities")).drop();
		new MysqlRepository(ds,new DefaultEntityMetaData("coll_person")).drop();

		// create collection, add repo, destroy and reload
		DefaultEntityMetaData personMD = new DefaultEntityMetaData("coll_person").setIdAttribute("email");
		personMD.addAttribute("email").setNillable(false);
		personMD.addAttribute("firstName");
		personMD.addAttribute("lastName");
		personMD.addAttribute("birthday").setDataType(MolgenisFieldTypes.DATE);
		personMD.addAttribute("height").setDataType(MolgenisFieldTypes.INT);
		personMD.addAttribute("active").setDataType(MolgenisFieldTypes.BOOL);

        //autowired ds
        //coll = new MysqlRepositoryCollection(ds);
		coll.add(personMD);

		// destroy and rebuild
		//coll = new MysqlRepositoryCollection(ds);
		Assert.assertNotNull(coll.getRepositoryByEntityName("coll_person"));

		MysqlRepository repo = (MysqlRepository) coll.getRepositoryByEntityName("coll_person");
		for (int i = 0; i < 10; i++)
		{
			Entity e = new MapEntity();
			e.set("email", i + "@localhost");
			repo.add(e);
		}

		// and again
		//coll = new MysqlRepositoryCollection(ds);
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
		dataSource.setJdbcUrl(MysqlRepositoryTestConstants.URL);
		dataSource.setUser("molgenis");
		dataSource.setPassword("molgenis");

		ds = dataSource;
	}
}
