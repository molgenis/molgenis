package org.molgenis.integrationtest.data.abstracts.query;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.stream.Collectors;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class AbstractAndQueryIT extends AbstractQueryIT
{
	@Override
	protected void testInt()
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(HEIGHT, 180).and().eq(FIRST_NAME, "john");
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(HEIGHT, 180).and().eq(ACTIVE, true);
		assertEquals(personsRepository.findAll(findAllQuery).collect(Collectors.toSet()), newHashSet(person1, person3));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(HEIGHT, 180).and().eq(LAST_NAME, "duck");
		assertEquals(personsRepository.count(countQuery), 1);
	}

	@Override
	protected void testDecimal()
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(ACCOUNT_BALANCE, 299.99).and().eq(FIRST_NAME, "john");
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(ACCOUNT_BALANCE, -0.70).and().eq(LAST_NAME, "doe");
		assertEquals(personsRepository.findAll(findAllQuery).collect(Collectors.toSet()), newHashSet(person2));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(ACCOUNT_BALANCE, 1000).and().eq(LAST_NAME, "duck");
		assertEquals(personsRepository.count(countQuery), 1);
	}

	@Override
	protected void testLong()
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(SERIAL_NUMBER, 374278348334L).and().eq(FIRST_NAME,
				"john");
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(SERIAL_NUMBER, 67986789879L).and().eq(LAST_NAME, "doe");
		assertEquals(personsRepository.findAll(findAllQuery).collect(Collectors.toSet()), newHashSet(person2));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(SERIAL_NUMBER, 23471900909L).and().eq(LAST_NAME, "duck");
		assertEquals(personsRepository.count(countQuery), 1);
	}

	@Override
	protected void testString()
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(FIRST_NAME, "john").and().eq(LAST_NAME, "doe");
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(LAST_NAME, "doe").and().eq(COUNTRY, "US");
		assertEquals(personsRepository.findAll(findAllQuery).collect(Collectors.toSet()), newHashSet(person1, person2));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(FIRST_NAME, "donald").and().eq(HEIGHT, 180);
		assertEquals(personsRepository.count(countQuery), 1);
	}

	@Override
	protected void testDate() throws ParseException
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(BIRTHDAY, dateFormat.parse("1980-06-07")).and()
				.eq(LAST_NAME, "doe");
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(BIRTHDAY, dateFormat.parse("1976-06-07")).and()
				.eq(HEIGHT, 180);
		assertEquals(personsRepository.findAll(findAllQuery).collect(toSet()), newHashSet(person1));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(BIRTHDAY, dateFormat.parse("1980-06-07")).and()
				.eq(FIRST_NAME, "jane");
		assertEquals(personsRepository.count(countQuery), 1);
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 06:06:06"))
				.and().eq(HEIGHT, 180);
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 06:06:06"))
				.and().eq(LAST_NAME, "doe");
		assertEquals(personsRepository.findAll(findAllQuery).collect(toSet()), newHashSet(person1));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 07:07:07"))
				.and().eq(ACTIVE, false);
		assertEquals(personsRepository.count(countQuery), 1);
	}

	@Override
	protected void testBool()
	{
		Query<Entity> query = new QueryImpl<Entity>().eq(ACTIVE, true).and().eq(HEIGHT, 180);
		assertTrue(newHashSet(person1, person3).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person1, person3));
		assertEquals(personsRepository.count(query), 2);
	}

	@Override
	protected void testMref()
	{
		Query<Entity> query = new QueryImpl<Entity>().eq(AUTHOR_OF, "MOLGENIS for dummies").and().eq(AUTHOR_OF,
				"Your database at the push of a button");
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person2));
		assertEquals(personsRepository.count(query), 1);
	}

	@Override
	protected void testXref()
	{
		Query<Entity> query = new QueryImpl<Entity>().eq(COUNTRY, "US").and().eq(LAST_NAME, "doe");
		assertTrue(newHashSet(person1, person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person1, person2));
		assertEquals(personsRepository.count(query), 2);
	}
}
