package org.molgenis.data.jpa;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

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

		repo.add(Arrays.asList(p1, p2));

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

	@AfterMethod
	public void afterMethod()
	{
		entityManager.getTransaction().rollback();
		entityManager.close();
	}

}
