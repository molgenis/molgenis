package org.molgenis.integrationtest.data.abstracts.query;

import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.Set;

import org.elasticsearch.common.collect.Sets;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class AbstractSearchQueryIT extends AbstractQueryIT
{

	@Override
	protected void testInt()
	{
		Query<Entity> query = new QueryImpl<>().search(HEIGHT, String.valueOf(165));
		Set<Entity> resultSet = Sets.newHashSet(person2);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testDecimal()
	{
		Query<Entity> query = new QueryImpl<>().search(ACCOUNT_BALANCE, "1000 ");
		Set<Entity> resultSet = Sets.newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testLong()
	{
		Query<Entity> query = new QueryImpl<>().search(SERIAL_NUMBER, "23471900909");
		Set<Entity> resultSet = Sets.newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testString()
	{
		Query<Entity> query = new QueryImpl<>().search(QUOTE, "is bit computer");
		Set<Entity> resultSet = Sets.newHashSet(person1, person2, person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testDate() throws ParseException
	{
		Query<Entity> query = new QueryImpl<>().search(BIRTHDAY, "1950-01-31");
		Set<Entity> resultSet = Sets.newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		// FIXME
		// Query<Entity> query = new QueryImpl<>().search(BIRTH_TIME, "1976-06-07 07:07:07");
		// Set<Entity> resultSet = Sets.newHashSet(person2);
		// assertTrue(resultSet.contains(personsRepository.findOne(query)));
		// assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		// assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testBool()
	{
		try
		{
			String value = "true";
			Query<Entity> query = new QueryImpl<Entity>().search(ACTIVE, value);
			assertTrue(newHashSet(person2).contains(personsRepository.findOne(query)));
			fail();
		} catch(MolgenisQueryException e) {
			// Expect a MolgenisQueryException
		}

	}

	@Override
	protected void testMref()
	{
		// FIXME
		// Query<Entity> query = new QueryImpl<>().search(AUTHOR_OF, "Your database at the push of a button");
		// System.out.println(personsRepository.findOne(query));
		// Set<Entity> resultSet = Sets.newHashSet(person2);
		// assertTrue(resultSet.contains(personsRepository.findOne(query)));
		// assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		// assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testXref()
	{
		// FIXME
		// Query<Entity> query = new QueryImpl<>().search(COUNTRY, "US");
		// System.out.println(personsRepository.findOne(query));
		// Set<Entity> resultSet = Sets.newHashSet(person1, person2);
		// assertTrue(resultSet.contains(personsRepository.findOne(query)));
		// assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		// assertEquals(personsRepository.count(query), resultSet.size());
	}

	protected void testAllFields()
	{
		Query<Entity> query = new QueryImpl<>().search("boolean");
		System.out.println(personsRepository.findOne(query));
		Set<Entity> resultSet = Sets.newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());

		query = new QueryImpl<>().search("1000");
		System.out.println(personsRepository.findOne(query));
		resultSet = Sets.newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

}
