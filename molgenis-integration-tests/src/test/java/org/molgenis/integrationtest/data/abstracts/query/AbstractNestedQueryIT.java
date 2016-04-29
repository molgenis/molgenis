package org.molgenis.integrationtest.data.abstracts.query;

import org.elasticsearch.common.collect.Sets;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

import java.text.ParseException;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AbstractNestedQueryIT extends AbstractQueryIT
{
	@Override void testInt()
	{
		Query<Entity> query = new QueryImpl<>().nest().eq(HEIGHT, 180).unnest();
		assertTrue(newHashSet(person1, person3).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person1, person3));
		assertEquals(personsRepository.count(query), 2);
	}

	@Override
	protected void testString()
	{
		Query<Entity> query = new QueryImpl<>().nest().eq(LAST_NAME, "doe").unnest();
		assertTrue(newHashSet(person1, person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person1, person2));
		assertEquals(personsRepository.count(query), 2);
	}

	@Override
	protected void testXref()
	{
		Query<Entity> query = new QueryImpl<>().nest().eq(COUNTRY, "US").unnest();
		assertTrue(newHashSet(person1, person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person1, person2));
		assertEquals(personsRepository.count(query), 2);
	}

	@Override
	protected void testDecimal()
	{
		Query<Entity> query = new QueryImpl<>().nest().eq(ACCOUNT_BALANCE, 1000.00).unnest();
		assertEquals(personsRepository.findOne(query), person3);
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person3));
		assertEquals(personsRepository.count(query), 1);
	}

	@Override
	protected void testLong()
	{
		Query<Entity> query = new QueryImpl<>().nest().eq(SERIAL_NUMBER, 67986789879L).unnest();
		assertEquals(personsRepository.findOne(query), person2);
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person2));
		assertEquals(personsRepository.count(query), 1);
	}

	@Override
	protected void testDate() throws ParseException
	{
		Query<Entity> query = new QueryImpl<>().nest().eq(BIRTHDAY, dateFormat.parse("1976-06-07")).unnest();
		assertEquals(personsRepository.findOne(query), person1);
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person1));
		assertEquals(personsRepository.count(query), 1);
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		Query<Entity> query = new QueryImpl<>().nest().eq(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 07:07:07")).unnest();
		assertEquals(personsRepository.findOne(query), person2);
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person2));
		assertEquals(personsRepository.count(query), 1);
	}

	@Override
	protected void testBool()
	{
		Query<Entity> query = new QueryImpl<>().nest().eq(ACTIVE, true).unnest();
		assertTrue(newHashSet(person1, person3).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person1, person3));
		assertEquals(personsRepository.count(query), 2);
	}

	@Override
	protected void testMref()
	{
		Query<Entity> query = new QueryImpl<>().nest().eq(AUTHOR_OF, "MOLGENIS for dummies").unnest();
		assertTrue(newHashSet(person1, person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person1, person2));
		assertEquals(personsRepository.count(query), 2);


		query = new QueryImpl<>().nest().eq(AUTHOR_OF, "MOLGENIS for dummies").and().eq(AUTHOR_OF, "Your database at the push of a button").unnest();
		assertTrue(Sets.newHashSet(person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person2));

		// FIXME PSQLException: ERROR: table name "authorOf" specified more than once
//		assertEquals(personsRepository.count(query), 1);
	}
}
