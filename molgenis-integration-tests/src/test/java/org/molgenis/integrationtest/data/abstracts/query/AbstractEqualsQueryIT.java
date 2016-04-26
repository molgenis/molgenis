package org.molgenis.integrationtest.data.abstracts.query;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.stream.Collectors;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

import com.google.common.collect.Sets;

public abstract class AbstractEqualsQueryIT extends AbstractQueryIT
{
	public void testIt() throws ParseException
	{
		// Repository<Entity> persons = getTestRepo();
		//
		// // string
		// Query<Entity> query = new QueryImpl<Entity>().eq("lastName", "doe");
		// assertTrue(Sets.newHashSet(person1, person2).contains(persons.findOne(query)));
		// assertEquals(persons.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1, person2));
		// assertEquals(persons.count(query), 2);
		//
		// // bool
		// query = new QueryImpl<Entity>().eq("active", true);
		// assertTrue(Sets.newHashSet(person1, person3).contains(persons.findOne(query)));
		// assertEquals(persons.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1, person3));
		// assertEquals(persons.count(query), 2);
		//
		// // date
		// query = new QueryImpl<Entity>().eq("birthday", dateFormat.parse("1976-06-07"));
		// assertEquals(persons.findOne(query), person1);
		// assertEquals(persons.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1));
		// assertEquals(persons.count(query), 1);
		//
		// // xref
		// query = new QueryImpl<Entity>().eq("country", "US");
		// assertTrue(Sets.newHashSet(person1, person2).contains(persons.findOne(query)));
		// assertEquals(persons.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1, person2));
		// assertEquals(persons.count(query), 2);

	}

	@Override
	protected void testInt()
	{
		Query<Entity> query = new QueryImpl<Entity>().eq("height", 180);
		assertTrue(Sets.newHashSet(person1, person3).contains(personsRepository.findOne(query)));
	}

	@Override
	protected void testString()
	{
		Query<Entity> query = new QueryImpl<Entity>().eq("lastName", "doe");
		assertTrue(Sets.newHashSet(person1, person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1, person2));
		assertEquals(personsRepository.count(query), 2);
	}

	@Override
	protected void testXref()
	{
		Query<Entity> query = new QueryImpl<Entity>().eq("country", "US");
		assertTrue(Sets.newHashSet(person1, person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1, person2));
		assertEquals(personsRepository.count(query), 2);
	}

	@Override
	protected void testDecimal()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void testLong()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void testDate() throws ParseException
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void testDateTime() throws ParseException
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void testBool()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void testMref()
	{
		Query<Entity> query = new QueryImpl<Entity>().eq("authorOf", "MOLGENIS for dummies");
		assertTrue(Sets.newHashSet(person1, person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1, person2));
		assertEquals(personsRepository.count(query), 2);
	}

}
