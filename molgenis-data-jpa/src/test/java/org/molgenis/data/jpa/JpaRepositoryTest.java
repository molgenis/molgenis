package org.molgenis.data.jpa;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.jpa.importer.EntityImportService;
import org.molgenis.data.support.ConvertingIterable;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.EntityUtils;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
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
		repo.delete(Arrays.asList(p));
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
	public void testFindAll()
	{
		Person p1 = new Person("Piet", "Paulusma");
		repo.add(p1);

		Person p2 = new Person("Paulus", "de Boskabouter");
		repo.add(p2);

		{
			Iterable<Entity> entities = repo.findAll(Arrays.asList((Object) p1.getId(), p2.getId()));
			assertEquals(entities, Arrays.asList(p1, p2));
		}
		{
			Iterable<Entity> entities = repo.findAll(Arrays.asList((Object) p2.getId(), p1.getId()));
			assertEquals(entities, Arrays.asList(p2, p1));
		}
	}

	@Test
	public void testFindAllTyped()
	{
		Person p1 = new Person("Piet", "Paulusma");
		repo.add(p1);

		Person p2 = new Person("Paulus", "de Boskabouter");
		repo.add(p2);

		Iterable<Entity> entities = repo.findAll(Arrays.asList((Object) p1.getId(), p2.getId()));
		Iterable<Person> it = new ConvertingIterable<Person>(Person.class, entities, null);
		assertEquals(Iterables.size(it), 2);
		assertTrue(Iterables.contains(it, p1));
		assertTrue(Iterables.contains(it, p2));
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
		repo.add(Arrays.asList(p));

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

	@Test
	public void testAndQuery()
	{
		// A and B
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		repo.add(Arrays.asList(p1, p2));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("firstName", "Piet").and().eq("lastName", "Paulusma"));
		assertEquals(Iterables.size(it), 1);
		assertEquals(it.iterator().next(), p1);
	}

	@Test
	public void testAndQueryFalseClause()
	{
		// A and B
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		repo.add(Arrays.asList(p1, p2));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("firstName", "Piet").and().eq("lastName", "Unknown"));
		assertEquals(Iterables.size(it), 0);
	}

	@Test
	public void testOrQuery()
	{
		// A or B
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		repo.add(Arrays.asList(p1, p2));

		Iterable<Entity> it = repo
				.findAll(new QueryImpl().eq("lastName", "Paulusma").or().eq("lastName", "de Boskabouter"));
		assertEquals(Iterables.size(it), 2);
		assertTrue(Iterables.contains(it, p1));
		assertTrue(Iterables.contains(it, p2));
	}

	@Test
	public void testOrQueryFalseClause()
	{
		// A or B
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		repo.add(Arrays.asList(p1, p2));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("lastName", "Paulusma").or().eq("lastName", "Unknown"));
		assertEquals(Iterables.size(it), 1);
		assertTrue(Iterables.contains(it, p1));
	}

	@Test
	public void testOrNestedAndQuery_TrueTrueTrue()
	{
		// A or (B and C) --> A = true, B = true, C = true
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("lastName", "Vaak").or().nest().eq("firstName", "Piet")
				.and().eq("lastName", "de Boskabouter").unnest());
		assertEquals(Iterables.size(it), 2);
		assertTrue(Iterables.contains(it, p2));
		assertTrue(Iterables.contains(it, p3));
	}

	@Test
	public void testOrNestedAndQuery_TrueFalseTrue()
	{
		// A or (B and C) --> A = true, B = false, C = true
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("lastName", "Vaak").or().nest().eq("firstName", "Unknown")
				.and().eq("lastName", "de Boskabouter").unnest());
		assertEquals(Iterables.size(it), 1);
		assertTrue(Iterables.contains(it, p3));
	}

	@Test
	public void testOrNestedAndQuery_TrueTrueFalse()
	{
		// A or (B and C) --> A = true, B = true, C = false
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("lastName", "Vaak").or().nest().eq("firstName", "Piet")
				.and().eq("lastName", "Unknown").unnest());
		assertEquals(Iterables.size(it), 1);
		assertTrue(Iterables.contains(it, p3));
	}

	@Test
	public void testOrNestedAndQuery_TrueFalseFalse()
	{
		// A or (B and C) --> A = true, B = false, C = false
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("lastName", "Vaak").or().nest().eq("firstName", "Unknown")
				.and().eq("lastName", "Unknown").unnest());
		assertEquals(Iterables.size(it), 1);
		assertTrue(Iterables.contains(it, p3));
	}

	@Test
	public void testOrNestedAndQuery_FalseTrueTrue()
	{
		// A or (B and C) --> A = false, B = true, C = true
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("lastName", "Unknown").or().nest().eq("firstName", "Piet")
				.and().eq("lastName", "de Boskabouter").unnest());
		assertEquals(Iterables.size(it), 1);
		assertTrue(Iterables.contains(it, p2));
	}

	@Test
	public void testOrNestedAndQuery_FalseFalseTrue()
	{
		// A or (B and C) --> A = false, B = false, C = true
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("lastName", "Unknown").or().nest()
				.eq("firstName", "Unknown").and().eq("lastName", "de Boskabouter").unnest());
		assertEquals(Iterables.size(it), 0);
	}

	@Test
	public void testOrNestedAndQuery_FalseFalseFalse()
	{
		// A or (B and C) --> A = false, B = false, C = false
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("lastName", "Unknown").or().nest()
				.eq("firstName", "Unknown").and().eq("lastName", "Unknown").unnest());
		assertEquals(Iterables.size(it), 0);
	}

	@Test
	public void testOrNestedAndQuery_FalseTrueFalse()
	{
		// A or (B and C) --> A = false, B = true, C = false
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("lastName", "Unknown").or().nest().eq("firstName", "Piet")
				.and().eq("lastName", "Unknown").unnest());
		assertEquals(Iterables.size(it), 0);
	}

	@Test
	public void testOrNestedAndQueryReversed()
	{
		// (B and C) or A
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().nest().eq("firstName", "Piet").and()
				.eq("lastName", "de Boskabouter").unnest().or().eq("lastName", "Vaak"));
		assertEquals(Iterables.size(it), 2);
		assertTrue(Iterables.contains(it, p2));
		assertTrue(Iterables.contains(it, p3));
	}

	@Test
	public void testOrNestedMultipleAndQuery()
	{
		// (A and B) or (C and D)
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().nest().eq("firstName", "Klaas").and().eq("lastName", "Vaak")
				.unnest().or().nest().eq("firstName", "Piet").and().eq("lastName", "de Boskabouter").unnest());
		assertEquals(Iterables.size(it), 2);
		assertTrue(Iterables.contains(it, p2));
		assertTrue(Iterables.contains(it, p3));
	}

	@Test
	public void testAndNestedOrQuery()
	{
		// (B or C) and A
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().nest().eq("lastName", "Paulusma").or()
				.eq("lastName", "de Boskabouter").unnest().and().eq("firstName", "Piet"));
		assertEquals(Iterables.size(it), 2);
		assertTrue(Iterables.contains(it, p1));
		assertTrue(Iterables.contains(it, p2));
	}

	@Test
	public void testAndNestedOrQueryFalse()
	{
		// (B or C) and A
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().nest().eq("lastName", "Unknown").or()
				.eq("lastName", "Unknown").unnest().and().eq("firstName", "Piet"));
		assertEquals(Iterables.size(it), 0);
	}

	@Test
	public void testAndNestedOrQueryReversed()
	{
		// A and (B or C)
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		Person p3 = new Person("Klaas", "Vaak");
		repo.add(Arrays.asList(p1, p2, p3));

		Iterable<Entity> it = repo.findAll(new QueryImpl().eq("firstName", "Piet").and().nest()
				.eq("lastName", "Paulusma").or().eq("lastName", "de Boskabouter").unnest());
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
	public void testSearchQuery()
	{
		Person p1 = new Person("Piet", "Paulusma");
		Person p2 = new Person("Piet", "de Boskabouter");
		repo.add(Arrays.asList(p1, p2));

		Iterable<Entity> it = repo.findAll(new QueryImpl().search("Bos"));
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

		Iterable<Entity> iter = repo.findAll(new QueryImpl().sort(new Sort("lastName", Direction.ASC)));
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

		Iterable<Entity> it = repo
				.findAll(new QueryImpl().pageSize(1).offset(1).sort(new Sort("lastName", Direction.ASC)));

		assertEquals(Iterables.size(it), 1);
		assertTrue(Iterables.contains(it, p3));
	}
}
