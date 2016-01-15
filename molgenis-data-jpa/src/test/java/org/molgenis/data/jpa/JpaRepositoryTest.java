package org.molgenis.data.jpa;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.jpa.importer.EntityImportService;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.EntityUtils;
import org.testng.annotations.Test;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class JpaRepositoryTest extends BaseJpaTest
{
	@Test
	public void testMrefQuery()
	{
		Person p = new Person("Piet", "Paulusma");
		repo.add(p);

		Person child1 = new Person("Child1", "Child1");
		repo.add(child1);

		Person child2 = new Person("Child2", "Child2");
		repo.add(child2);

		p.getChildren().add(child1);
		p.getChildren().add(child2);
		repo.update(p);

		Query q = new QueryImpl().in("children", Lists.newArrayList(child1));
		Person found = EntityUtils.convert(repo.findOne(q), Person.class, null);
		assertEquals(p, found);
	}

	@Test
	public void testAddAndretrieve()
	{
		Person p = new Person("Piet", "Paulusma");
		repo.add(p);

		assertNotNull(p.getId());

		Entity retrieved = repo.findOne(p.getId());
		assertNotNull(retrieved);
		assertTrue(retrieved instanceof Person);
		assertEquals(retrieved.get("firstName"), p.getFirstName());
		assertEquals(retrieved.get("lastName"), p.getLastName());
	}

	@Test
	public void testCount()
	{
		repo.add(Arrays.asList(new Person("1", "1"), new Person("2", "2")).stream());
		assertEquals(repo.count(), 2);
	}

	@Test
	public void testQueryCount()
	{
		repo.add(Arrays.asList(new Person("1", "1"), new Person("2", "2")).stream());
		assertEquals(repo.count(new QueryImpl().eq("firstName", "1")), 1);
	}

	@Test
	public void testDelete()
	{
		Person p = new Person("Piet", "Paulusma");
		repo.add(p);
		Object id = p.getIdValue();
		repo.delete(p);
		assertNull(repo.findOne(id));
	}

	@Test
	public void testDeleteIterable()
	{
		Person p = new Person("Piet", "Paulusma");
		repo.add(p);
		Object id = p.getIdValue();
		repo.delete(Arrays.asList(p).stream());
		assertNull(repo.findOne(id));
	}

	@Test
	public void testDeleteAll()
	{
		Person p = new Person("Piet", "Paulusma");
		repo.add(p);
		Object id = p.getIdValue();
		repo.deleteAll();
		assertNull(repo.findOne(id));
	}

	@Test
	public void testDeleteById()
	{
		Person p = new Person("Piet", "Paulusma");
		repo.add(p);
		Object id = p.getIdValue();
		repo.deleteById(p.getId());
		assertNull(repo.findOne(id));
	}

	@Test
	public void testDeleteByIdIterable()
	{
		Person p = new Person("Piet", "Paulusma");
		repo.add(p);
		Object id = p.getIdValue();
		repo.deleteById(Arrays.asList(id));
		assertNull(repo.findOne(p.getId()));
	}

	@Test
	public void testFindOne()
	{
		Person p = new Person("Piet", "Paulusma");
		repo.add(p);
		Entity e = repo.findOne(p.getId());
		assertNotNull(e);
		assertEquals(p, e);
	}

	@Test
	public void testFindOneTyped()
	{
		Person p = new Person("Piet", "Paulusma");
		repo.add(p);
		Person e = EntityUtils.convert(repo.findOne(p.getId()), Person.class, null);
		assertNotNull(e);
		assertEquals(p, e);
	}

	@Test
	public void testIterator()
	{
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Paulus", "de Boskabouter");
		repo.add(Arrays.asList(p1, p2).stream());

		assertNotNull(repo.iterator());
		assertEquals(Iterators.size(repo.iterator()), 2);
		assertTrue(Iterators.contains(repo.iterator(), p1));
		assertTrue(Iterators.contains(repo.iterator(), p2));
	}

	@Test
	public void testUpdate()
	{
		Person p = new Person("Piet", "Paulusma");
		repo.add(p);

		Person e = EntityUtils.convert(repo.findOne(p.getId()), Person.class, null);
		e.setLastName("XXX");
		repo.update(e);

		Person e1 = EntityUtils.convert(repo.findOne(p.getId()), Person.class, null);
		assertNotNull(e1);
		assertEquals(e.getLastName(), "XXX");
	}

	@Test
	public void testUpdatIterable()
	{
		Person p = new Person("Piet", "Paulusma");
		repo.add(Arrays.asList(p).stream());

		Person e = EntityUtils.convert(repo.findOne(p.getId()), Person.class, null);
		e.setLastName("XXX");
		repo.update(e);

		Person e1 = EntityUtils.convert(repo.findOne(p.getId()), Person.class, null);
		assertNotNull(e1);
		assertEquals(e.getLastName(), "XXX");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testImportAdd()
	{
		Entity e = new MapEntity("id");
		e.set("firstName", "Piet");
		e.set("lastName", "Paulusma");

		EntityImportService eis = new EntityImportService();

		eis.update(repo, Arrays.asList(e), DatabaseAction.ADD, "firstName");
		assertEquals(repo.count(), 1);

		Entity e1 = new MapEntity("id");
		e1.set("firstName", "Piet");
		e1.set("lastName", "Paulusma");
		eis.update(repo, Arrays.asList(e1), DatabaseAction.ADD, "firstName");
	}

	@Test
	public void testImportAddUpdateExisting()
	{
		Entity e = new MapEntity("id");
		e.set("firstName", "Piet");
		e.set("lastName", "Paulusma");

		EntityImportService eis = new EntityImportService();

		eis.update(repo, Arrays.asList(e), DatabaseAction.ADD_UPDATE_EXISTING, "firstName");
		assertEquals(repo.count(), 1);

		Entity e1 = new MapEntity("id");
		e1.set("id", e.getIdValue());
		e1.set("firstName", "Piet");
		e1.set("lastName", "XXX");
		eis.update(repo, Arrays.asList(e1), DatabaseAction.ADD_UPDATE_EXISTING, "firstName");
		assertEquals(repo.count(), 1);
		assertEquals(repo.iterator().next().getString("lastName"), "XXX");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testImportUpdateMissing()
	{
		Entity e = new MapEntity("id");
		e.set("firstName", "Piet");
		e.set("lastName", "Paulusma");

		EntityImportService eis = new EntityImportService();
		eis.update(repo, Arrays.asList(e), DatabaseAction.UPDATE, "firstName");
	}

	@Test
	public void testImportUpdate()
	{
		Entity e = new MapEntity("id");
		e.set("firstName", "Piet");
		e.set("lastName", "Paulusma");
		repo.add(e);

		Entity e1 = new MapEntity("id");
		e1.set("id", e.getIdValue());
		e1.set("firstName", "Piet");
		e1.set("lastName", "XXX");

		EntityImportService eis = new EntityImportService();
		eis.update(repo, Arrays.asList(e1), DatabaseAction.UPDATE, "firstName");
		assertEquals(repo.count(), 1);
		assertEquals(repo.iterator().next().getString("lastName"), "XXX");
	}
}
