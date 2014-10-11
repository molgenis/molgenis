package org.molgenis.data.mysql.meta;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.molgenis.AppConfig;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.meta.AttributeMetaDataRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.mysql.meta.MysqlEntityMetaDataRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

@ContextConfiguration(classes = AppConfig.class)
public class EntityMetaDataRepositoryTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private AttributeMetaDataRepository attributeMetaDataRepository;

	@Autowired
	private MysqlEntityMetaDataRepository entityMetaDataRepository;

	@Autowired
	private MysqlPackageRepository packageRepository;

	@Autowired
	private MysqlRepositoryCollection coll;

	@BeforeClass
	@BeforeMethod
	@AfterClass
	public void reset()
	{
		coll.recreateMetaDataRepositories();
	}

	@Test
	public void addAndGetEntityMetaData()
	{
		DefaultEntityMetaData test = new DefaultEntityMetaData("test");
		entityMetaDataRepository.addEntityMetaData(test);

		DefaultEntityMetaData extendsTest = new DefaultEntityMetaData("extendstest");
		extendsTest.setExtends(test);
		entityMetaDataRepository.addEntityMetaData(extendsTest);

		EntityMetaData retrieved = entityMetaDataRepository.getEntityMetaData("extendstest");
		assertNotNull(retrieved);
		assertEquals(retrieved.getName(), "extendstest");
		assertEquals(retrieved.getExtends(), test);
	}

	@Test
	public void getEntityMetaDataNotFound()
	{
		assertNull(entityMetaDataRepository.getEntityMetaData("unknown"));
	}

	@Test
	public void getEntityMetaDatas()
	{
		DefaultEntityMetaData test = new DefaultEntityMetaData("test");
		entityMetaDataRepository.addEntityMetaData(test);

		DefaultEntityMetaData test1 = new DefaultEntityMetaData("test1");
		entityMetaDataRepository.addEntityMetaData(test1);

		DefaultEntityMetaData test2 = new DefaultEntityMetaData("test2");
		entityMetaDataRepository.addEntityMetaData(test2);

		List<EntityMetaData> meta = Lists.newArrayList(entityMetaDataRepository.getEntityMetaDatas());
		assertNotNull(meta);
		assertEquals(meta.size(), 3);
		assertTrue(meta.contains(test));
		assertTrue(meta.contains(test1));
		assertTrue(meta.contains(test2));
	}

	@Test
	public void getEntityMetaDatasForPackage()
	{

		Package p1 = new PackageImpl("p1", "Package1");
		packageRepository.addPackage(p1);

		Package p2 = new PackageImpl("p2", "Package2", p1);
		packageRepository.addPackage(p2);

		DefaultEntityMetaData test = new DefaultEntityMetaData("test");
		entityMetaDataRepository.addEntityMetaData(test);

		DefaultEntityMetaData test1 = new DefaultEntityMetaData("test1", p1);
		entityMetaDataRepository.addEntityMetaData(test1);

		DefaultEntityMetaData test2 = new DefaultEntityMetaData("test2", p2);
		entityMetaDataRepository.addEntityMetaData(test2);

		DefaultEntityMetaData test3 = new DefaultEntityMetaData("test3", p2);
		entityMetaDataRepository.addEntityMetaData(test3);

		assertEquals(entityMetaDataRepository.getPackageEntityMetaDatas("p1_p2"), Arrays.asList(test2, test3));
	}
}
