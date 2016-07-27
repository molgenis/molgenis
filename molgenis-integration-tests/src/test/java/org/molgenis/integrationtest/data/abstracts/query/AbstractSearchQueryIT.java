package org.molgenis.integrationtest.data.abstracts.query;

import static java.util.stream.Collectors.toSet;
import static org.elasticsearch.common.collect.Sets.newHashSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.text.ParseException;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class AbstractSearchQueryIT extends AbstractQueryIT
{

	@Override
	protected void testInt()
	{
		Query<Entity> query = new QueryImpl<>().search(HEIGHT, String.valueOf(165));
		Set<Entity> resultSet = newHashSet(person2);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testDecimal()
	{
		Query<Entity> query = new QueryImpl<>().search(ACCOUNT_BALANCE, "1000 ");
		Set<Entity> resultSet = newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testLong()
	{
		Query<Entity> query = new QueryImpl<>().search(SERIAL_NUMBER, "23471900909");
		Set<Entity> resultSet = newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testString()
	{
		Query<Entity> query = new QueryImpl<>().search(QUOTE, "is bit computer");
		Set<Entity> resultSet = newHashSet(person1, person2, person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testDate() throws ParseException
	{
		Query<Entity> query = new QueryImpl<>().search(BIRTHDAY, "1950-01-31");
		Set<Entity> resultSet = newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		// FIXME Value generated just like the ElasticSearch QueryGeneratorTest.generateOneQueryRuleSearchOneFieldDateTime
		// FIXME but still does not work
		//		String value = getDateFormat().parse("1976-06-07T07:07:07+0100").toString();
		//
		//	 	Query<Entity> query = new QueryImpl<>().search(BIRTH_TIME, value);
		//	 	Set<Entity> resultSet = Sets.newHashSet(person2);
		// 		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		//	 	assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		//	 	assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testBool()
	{
		try
		{
			personsRepository.findOne(new QueryImpl<Entity>().search(ACTIVE, "true"));
			fail();
		}
		catch (MolgenisQueryException e)
		{
			// Expect a MolgenisQueryException
		}

	}

	@Override
	protected void testMref()
	{
		// FIXME Returns null, but should find person 2
		//		 Query<Entity> query = new QueryImpl<>().search(AUTHOR_OF, "Your database at the push of a button");
		//		 System.out.println(personsRepository.findOne(query));
		//
		//		 assertTrue(newHashSet(person2).contains(personsRepository.findOne(query)));
		//		 assertEquals(personsRepository.findAll(query).collect(toSet()),  newHashSet(person2));
		//		 assertEquals(personsRepository.count(query), 1);
	}

	@Override
	protected void testXref()
	{
		// FIXME Returns null, but should find person1 and person2
		//		 Query<Entity> query = new QueryImpl<>().search(COUNTRY, "US");
		//		 System.out.println(personsRepository.findOne(query));
		//
		//	 	assertTrue(newHashSet(person1, person2).contains(personsRepository.findOne(query)));
		//	 	assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person1, person2));
		//	 	assertEquals(personsRepository.count(query), 2);
	}

	protected void testAllFields()
	{
		Query<Entity> query = new QueryImpl<>().search("boolean");
		System.out.println(personsRepository.findOne(query));
		Set<Entity> resultSet = newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());

		query = new QueryImpl<>().search("1000");
		System.out.println(personsRepository.findOne(query));
		resultSet = newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

}
