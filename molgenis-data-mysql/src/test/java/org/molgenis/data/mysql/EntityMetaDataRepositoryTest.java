package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.molgenis.AppConfig;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AppConfig.class)
public class EntityMetaDataRepositoryTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private EntityMetaDataRepository entityMetaDataRepository;

	@BeforeMethod
	public void beforeMethod()
	{
		entityMetaDataRepository.deleteAll();
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
	public void getEntityMetaDatas()
	{
		DefaultEntityMetaData test = new DefaultEntityMetaData("test");
		entityMetaDataRepository.addEntityMetaData(test);

		DefaultEntityMetaData test1 = new DefaultEntityMetaData("test1");
		entityMetaDataRepository.addEntityMetaData(test1);

		DefaultEntityMetaData test2 = new DefaultEntityMetaData("test2");
		entityMetaDataRepository.addEntityMetaData(test2);

		List<DefaultEntityMetaData> meta = entityMetaDataRepository.getEntityMetaDatas();
		assertNotNull(meta);
		assertEquals(meta.size(), 3);
		assertTrue(meta.contains(test));
		assertTrue(meta.contains(test1));
		assertTrue(meta.contains(test2));
	}
}
