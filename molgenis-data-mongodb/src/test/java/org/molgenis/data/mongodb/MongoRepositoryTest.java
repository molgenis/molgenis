package org.molgenis.data.mongodb;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class MongoRepositoryTest extends AbstractMongoRepositoryTest
{
	private MongoRepository repo;

	@Override
	@BeforeMethod
	public void beforeMethod()
	{
		super.beforeMethod();

		DefaultEntityMetaData meta = new DefaultEntityMetaData("person");
		meta.addAttribute("name").setIdAttribute(true);
		meta.addAttribute("age").setDataType(MolgenisFieldTypes.INT);
		repo = createRepo(meta);
	}

	@Test
	public void testAddAndFindOne()
	{
		Entity piet = new MapEntity("name", "Piet");
		piet.set("age", 30);

		repo.add(piet);

		Entity entity = repo.findOne("Piet");
		assertNotNull(entity);
		assertEquals(entity.get("name"), "Piet");
		assertEquals(entity.get("age"), 30);
	}

	@Test
	public void testIterator()
	{
		add(new MapEntity("name", "Piet"), new MapEntity("name", "Klaas"), new MapEntity("name", "Jaap"));
		assertEquals(Iterators.size(repo.iterator()), 3);
	}

	@Test
	public void testFindAllIds()
	{
		add(new MapEntity("name", "Piet"), new MapEntity("name", "Klaas"), new MapEntity("name", "Jaap"));
		Iterable<Entity> found = repo.findAll(Arrays.<Object> asList("Klaas", "Jaap"));
		assertEquals(Iterables.size(found), 2);
	}

	@Test
	public void testUpdate()
	{
		Entity piet = new MapEntity("name", "Piet");
		piet.set("age", 30);

		add(piet);

		piet.set("age", 20);
		repo.update(piet);

		Entity entity = repo.findOne("Piet");
		assertNotNull(entity);
		assertEquals(entity.get("name"), "Piet");
		assertEquals(entity.get("age"), 20);
	}

	protected void add(Entity... entities)
	{
		for (Entity entity : entities)
		{
			repo.add(entity);
		}
	}
}
