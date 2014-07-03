package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.util.Arrays;

import org.molgenis.AppConfig;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AppConfig.class)
public class MysqlUpdateInternalTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MysqlRepositoryCollection repositoryCollection;
	private MysqlRepository repo;
	private final DefaultEntityMetaData meta;

	public MysqlUpdateInternalTest()
	{
		meta = new DefaultEntityMetaData("Person");
		meta.addAttribute("id").setIdAttribute(true).setNillable(false);
		meta.addAttribute("name");
	}

	@BeforeMethod
	public void beforeMethod()
	{
		repo = repositoryCollection.add(meta);
	}

	@AfterMethod
	public void afterMethod()
	{
		repositoryCollection.drop(meta);
	}

	@Test
	public void testAdd()
	{
		Entity entity = new MapEntity();
		entity.set("id", "123");
		entity.set("name", "piet");

		repo.updateInternal(Arrays.asList(entity), DatabaseAction.ADD);
		assertEquals(repo.count(), 1);

		Entity entity1 = new MapEntity();
		entity1.set("id", "123");
		entity1.set("name", "klaas");

		try
		{
			repo.updateInternal(Arrays.asList(entity1), DatabaseAction.ADD);
			fail("Should have thrown MolgenisDataException");
		}
		catch (MolgenisDataException e)
		{
			// expected
			assertEquals(repo.count(), 1);
		}
	}

	@Test
	public void testAddIgnoreExisting()
	{
		Entity entity = new MapEntity();
		entity.set("id", "123");
		entity.set("name", "piet");
		repo.updateInternal(Arrays.asList(entity), DatabaseAction.ADD_IGNORE_EXISTING);

		Entity entity1 = new MapEntity();
		entity1.set("id", "123");// Same id, should ignore it
		entity1.set("name", "klaas");
		repo.updateInternal(Arrays.asList(entity1), DatabaseAction.ADD_IGNORE_EXISTING);

		assertEquals(repo.count(), 1);
	}

	@Test
	public void testAddUpdateExisting()
	{
		Entity entity1 = new MapEntity();
		entity1.set("id", "123");
		entity1.set("name", "piet");
		repo.updateInternal(Arrays.asList(entity1), DatabaseAction.ADD);

		Entity entity2 = new MapEntity();
		entity2.set("id", "123");// Same id, should update it
		entity2.set("name", "klaas");

		Entity entity3 = new MapEntity();
		entity3.set("id", "345");// New id, should add it
		entity3.set("name", "jaap");

		repo.updateInternal(Arrays.asList(entity2, entity3), DatabaseAction.ADD_UPDATE_EXISTING);

		assertEquals(repo.count(), 2);

		Entity updated = repo.findOne("123");
		assertNotNull(updated);
		assertEquals(updated.get("name"), "klaas");
	}

	@Test
	public void testAddUpdate()
	{
		Entity entity = new MapEntity();
		entity.set("id", "123");
		entity.set("name", "piet");
		repo.updateInternal(Arrays.asList(entity), DatabaseAction.ADD);

		Entity entity1 = new MapEntity();
		entity1.set("id", "123");// Same id, should update it
		entity1.set("name", "klaas");
		repo.updateInternal(Arrays.asList(entity1), DatabaseAction.UPDATE);

		assertEquals(repo.count(), 1);
		Entity updated = repo.findOne("123");
		assertNotNull(updated);
		assertEquals(updated.get("name"), "klaas");

		Entity entity3 = new MapEntity();
		entity3.set("id", "456");// New id, should throw
		entity3.set("name", "jaap");

		try
		{
			repo.updateInternal(Arrays.asList(entity3), DatabaseAction.UPDATE);
			fail("Should have thrown MolgenisDataException");
		}
		catch (MolgenisDataException e)
		{
			// expected
			assertEquals(repo.count(), 1);
		}
	}

	@Test
	public void testAddUpdateIgnoreMissing()
	{
		Entity entity = new MapEntity();
		entity.set("id", "123");
		entity.set("name", "piet");
		repo.updateInternal(Arrays.asList(entity), DatabaseAction.ADD);

		Entity entity1 = new MapEntity();
		entity1.set("id", "123");// Same id, should update it
		entity1.set("name", "klaas");

		Entity entity2 = new MapEntity();
		entity2.set("id", "345");// New id, should ignore it
		entity2.set("name", "jaap");

		repo.updateInternal(Arrays.asList(entity1), DatabaseAction.UPDATE_IGNORE_MISSING);
		assertEquals(repo.count(), 1);
		Entity updated = repo.findOne("123");
		assertNotNull(updated);
		assertEquals(updated.get("name"), "klaas");
	}

	@Test
	public void testRemove()
	{
		Entity entity = new MapEntity();
		entity.set("id", "123");
		entity.set("name", "piet");

		repo.updateInternal(Arrays.asList(entity), DatabaseAction.ADD);
		assertEquals(repo.count(), 1);

		Entity entity1 = new MapEntity();
		entity1.set("id", "123");

		repo.updateInternal(Arrays.asList(entity1), DatabaseAction.REMOVE);
		assertEquals(repo.count(), 0);

		try
		{
			repo.updateInternal(Arrays.asList(entity1), DatabaseAction.REMOVE);
			fail("Should have thrown MolgenisDataException");
		}
		catch (MolgenisDataException e)
		{
			// expected
		}
	}

	@Test
	public void testRemoveIgnoreMissing()
	{
		Entity entity = new MapEntity();
		entity.set("id", "123");
		entity.set("name", "piet");

		repo.updateInternal(Arrays.asList(entity), DatabaseAction.ADD);
		assertEquals(repo.count(), 1);

		Entity entity1 = new MapEntity();
		entity1.set("id", "345");

		Entity entity2 = new MapEntity();
		entity2.set("id", "123");

		repo.updateInternal(Arrays.asList(entity1, entity2), DatabaseAction.REMOVE_IGNORE_MISSING);
		assertEquals(repo.count(), 0);
	}

}
