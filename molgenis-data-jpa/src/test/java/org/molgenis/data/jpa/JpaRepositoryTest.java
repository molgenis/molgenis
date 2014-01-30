package org.molgenis.data.jpa;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class JpaRepositoryTest
{
	private EntityManager entityManager;
	private JpaRepository repo;

	@BeforeMethod
	public void beforeMethod()
	{
		entityManager = Persistence.createEntityManagerFactory("molgenis").createEntityManager();
		repo = new JpaRepository(entityManager, Person.class, new PersonMetaData());
		entityManager.getTransaction().begin();
	}

	@Test
	public void testAddAndretrieve()
	{
		Person p = new Person("Piet", "Paulusma");
		Integer id = repo.add(p);

		assertNotNull(id);

		Entity retrieved = repo.findOne(id);
		assertNotNull(retrieved);
		assertTrue(retrieved instanceof Person);
		assertEquals(retrieved.get("firstName"), p.getFirstName());
		assertEquals(retrieved.get("lastName"), p.getLastName());
	}

	@Test
	public void testCount()
	{
		repo.add(Arrays.asList(new Person("1", "1"), new Person("2", "2")));
		assertEquals(repo.count(), 2);
	}

	@Test
	public void testQueryCount()
	{
		repo.add(Arrays.asList(new Person("1", "1"), new Person("2", "2")));
		assertEquals(repo.count(new QueryImpl().eq("firstName", "1")), 1);
	}

	@Test
	public void testDelete()
	{
		Person p = new Person("Piet", "Paulusma");
		Integer id = repo.add(p);
		repo.delete(p);
		assertNull(repo.findOne(id));
	}

	@Test
	public void testDeleteIterable()
	{
		Person p = new Person("Piet", "Paulusma");
		Integer id = repo.add(p);
		repo.delete(Arrays.asList(p));
		assertNull(repo.findOne(id));
	}

	@Test
	public void testDeleteAll()
	{
		Person p = new Person("Piet", "Paulusma");
		Integer id = repo.add(p);
		repo.deleteAll();
		assertNull(repo.findOne(id));
	}

	@Test
	public void testDeleteById()
	{
		Person p = new Person("Piet", "Paulusma");
		Integer id = repo.add(p);
		repo.deleteById(id);
		assertNull(repo.findOne(id));
	}

	@Test
	public void testDeleteByIdIterable()
	{
		Person p = new Person("Piet", "Paulusma");
		Integer id = repo.add(p);
		repo.deleteById(Arrays.asList(id));
		assertNull(repo.findOne(id));
	}

	@Test
	public void testFindAll()
	{
		Person p1 = new Person("Piet", "Paulusma");
		Integer id1 = repo.add(p1);

		Person p2 = new Person("Paulus", "de Boskabouter");
		Integer id2 = repo.add(p2);

		Iterable<Entity> it = repo.findAll(Arrays.asList(id1, id2));
		assertEquals(Iterables.size(it), 2);
		assertTrue(Iterables.contains(it, p1));
		assertTrue(Iterables.contains(it, p2));
	}

	@Test
	public void testFindAllTyped()
	{
		Person p1 = new Person("Piet", "Paulusma");
		Integer id1 = repo.add(p1);

		Person p2 = new Person("Paulus", "de Boskabouter");
		Integer id2 = repo.add(p2);

		repo.add(Arrays.asList(p1, p2));

		Iterable<Person> it = repo.findAll(Arrays.asList(id1, id2), Person.class);
		assertEquals(Iterables.size(it), 2);
		assertTrue(Iterables.contains(it, p1));
		assertTrue(Iterables.contains(it, p2));
	}

	@Test
	public void testFindOne()
	{
		Person p = new Person("Piet", "Paulusma");
		Integer id = repo.add(p);
		Entity e = repo.findOne(id);
		assertNotNull(e);
		assertEquals(p, e);
	}

	@Test
	public void testFindOneTyped()
	{
		Person p = new Person("Piet", "Paulusma");
		Integer id = repo.add(p);
		Person e = repo.findOne(id, Person.class);
		assertNotNull(e);
		assertEquals(p, e);
	}

	@Test
	public void testIterator()
	{
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Paulus", "de Boskabouter");
		repo.add(Arrays.asList(p1, p2));

		assertNotNull(repo.iterator());
		assertEquals(Iterators.size(repo.iterator()), 2);
		assertTrue(Iterators.contains(repo.iterator(), p1));
		assertTrue(Iterators.contains(repo.iterator(), p2));
	}

	@Test
	public void testUpdate()
	{
		Person p = new Person("Piet", "Paulusma");
		Integer id = repo.add(p);

		Person e = repo.findOne(id, Person.class);
		e.setLastName("XXX");
		repo.update(e);

		Person e1 = repo.findOne(id, Person.class);
		assertNotNull(e1);
		assertEquals(e.getLastName(), "XXX");
	}

	@Test
	public void testUpdatIterable()
	{
		Person p = new Person("Piet", "Paulusma");
		repo.add(Arrays.asList(p));

		Person e = repo.findOne(p.getId(), Person.class);
		e.setLastName("XXX");
		repo.update(e);

		Person e1 = repo.findOne(p.getId(), Person.class);
		assertNotNull(e1);
		assertEquals(e.getLastName(), "XXX");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testImportAdd()
	{
		Entity e = new MapEntity("id");
		e.set("firstName", "Piet");
		e.set("lastName", "Paulusma");

		repo.update(Arrays.asList(e), DatabaseAction.ADD, "firstName");
		assertEquals(repo.count(), 1);

		Entity e1 = new MapEntity("id");
		e1.set("firstName", "Piet");
		e1.set("lastName", "Paulusma");
		repo.update(Arrays.asList(e1), DatabaseAction.ADD, "firstName");
	}

	@Test
	public void testImportAddIgnoreExisting()
	{
		Entity e = new MapEntity("id");
		e.set("firstName", "Piet");
		e.set("lastName", "Paulusma");

		repo.update(Arrays.asList(e), DatabaseAction.ADD_IGNORE_EXISTING, "firstName");
		assertEquals(repo.count(), 1);

		Entity e1 = new MapEntity("id");
		e1.set("firstName", "Piet");
		e1.set("lastName", "Paulusma");
		repo.update(Arrays.asList(e1), DatabaseAction.ADD_IGNORE_EXISTING, "firstName");
		assertEquals(repo.count(), 1);
	}

	@Test
	public void testImportAddUpdateIgnoreExisting()
	{
		Entity e = new MapEntity("id");
		e.set("firstName", "Piet");
		e.set("lastName", "Paulusma");

		repo.update(Arrays.asList(e), DatabaseAction.ADD_UPDATE_EXISTING, "firstName");
		assertEquals(repo.count(), 1);

		Entity e1 = new MapEntity("id");
		e1.set("firstName", "Piet");
		e1.set("lastName", "XXX");
		repo.update(Arrays.asList(e1), DatabaseAction.ADD_UPDATE_EXISTING, "firstName");
		assertEquals(repo.count(), 1);
		assertEquals(repo.iterator(Person.class).iterator().next().getLastName(), "XXX");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testImportUpdateMissing()
	{
		Entity e = new MapEntity("id");
		e.set("firstName", "Piet");
		e.set("lastName", "Paulusma");

		repo.update(Arrays.asList(e), DatabaseAction.UPDATE, "firstName");
	}

	@Test
	public void testImportUpdate()
	{
		Entity e = new MapEntity("id");
		e.set("firstName", "Piet");
		e.set("lastName", "Paulusma");
		repo.add(e);

		Entity e1 = new MapEntity("id");
		e1.set("firstName", "Piet");
		e1.set("lastName", "XXX");
		repo.update(Arrays.asList(e1), DatabaseAction.UPDATE, "firstName");
		assertEquals(repo.count(), 1);
		assertEquals(repo.iterator(Person.class).iterator().next().getLastName(), "XXX");
	}

	@Test
	public void testImportUpdateIgnoreMissing()
	{
		Entity e1 = new MapEntity("id");
		e1.set("firstName", "Piet");
		e1.set("lastName", "XXX");
		repo.update(Arrays.asList(e1), DatabaseAction.UPDATE_IGNORE_MISSING, "firstName");
		assertEquals(repo.count(), 0);
	}

	@Test
	public void testRemove()
	{
		Entity e = new MapEntity("id");
		e.set("firstName", "Piet");
		e.set("lastName", "Paulusma");
		repo.add(e);

		Entity e1 = new MapEntity("id");
		e1.set("firstName", "Piet");
		e1.set("lastName", "XXXX");
		repo.update(Arrays.asList(e1), DatabaseAction.REMOVE, "firstName");
		assertEquals(repo.count(), 0);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testRemoveMissing()
	{
		Entity e1 = new MapEntity("id");
		e1.set("firstName", "Piet");
		e1.set("lastName", "XXXX");
		repo.update(Arrays.asList(e1), DatabaseAction.REMOVE, "firstName");
	}

	@Test
	public void testRemoveIgnoreMissing()
	{
		Entity e = new MapEntity("id");
		e.set("firstName", "Piet");
		e.set("lastName", "Paulusma");
		repo.add(e);

		Entity e1 = new MapEntity("id");
		e1.set("firstName", "Piet");
		e1.set("lastName", "XXXX");
		repo.update(Arrays.asList(e1), DatabaseAction.REMOVE_IGNORE_MISSING, "firstName");
		assertEquals(repo.count(), 0);
	}

	@Test
	public void testRemoveIgnoreMissingMissing()
	{
		Entity e1 = new MapEntity("id");
		e1.set("firstName", "Piet");
		e1.set("lastName", "XXXX");
		repo.update(Arrays.asList(e1), DatabaseAction.REMOVE_IGNORE_MISSING, "firstName");
		assertEquals(repo.count(), 0);
	}

	@Test
	public void testAndQuery()
	{
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		repo.add(Arrays.asList(p1, p2));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("firstName", "Piet").and().eq("lastName", "Paulusma"));
		assertEquals(Iterables.size(it), 1);
		assertEquals(it.iterator().next(), p1);
	}

	@Test
	public void testOrQuery()
	{
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		repo.add(Arrays.asList(p1, p2));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("lastName", "Paulusma").or()
				.eq("lastName", "de Boskabouter"));
		assertEquals(Iterables.size(it), 2);
		assertTrue(Iterables.contains(it, p1));
		assertTrue(Iterables.contains(it, p2));
	}

	@Test
	public void testInQuery()
	{
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		repo.add(Arrays.asList(p1, p2));

		Iterable<Entity> it = repo.findAll(new QueryImpl().in("firstName", Arrays.asList("Piet", "Klaas", "Jaap")));
		assertEquals(Iterables.size(it), 2);
		assertTrue(Iterables.contains(it, p1));
		assertTrue(Iterables.contains(it, p2));
	}

	@Test
	public void testLikeQuery()
	{
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		repo.add(Arrays.asList(p1, p2));

		Iterable<Entity> it = repo.findAll(new QueryImpl().like("lastName", "de"));
		assertEquals(Iterables.size(it), 1);
		assertTrue(Iterables.contains(it, p2));
	}

	@Test
	public void testSort()
	{
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> iter = repo.findAll(new QueryImpl().sort(new Sort(Direction.ASC, "lastName")));
		assertEquals(Iterables.size(iter), 3);

		Iterator<Entity> it = iter.iterator();
		assertEquals(p1, it.next());
		assertEquals(p3, it.next());
		assertEquals(p2, it.next());
	}

	@Test
	public void testPageSizeAndOffset()
	{
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().pageSize(1).offset(1)
				.sort(new Sort(Direction.ASC, "lastName")));
		System.out.println(it);
		assertEquals(Iterables.size(it), 1);
		assertTrue(Iterables.contains(it, p3));
	}

	@AfterMethod
	public void afterMethod()
	{
		entityManager.getTransaction().rollback();
		entityManager.close();
	}

}
