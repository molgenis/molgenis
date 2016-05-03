package org.molgenis.integrationtest.data.abstracts.query;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class AbstractOrQueryIT extends AbstractQueryIT
{
	@Override
	protected void testInt()
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(HEIGHT, 123).or().eq(HEIGHT, 165);
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(HEIGHT, 180).or().eq(HEIGHT, 165);
		assertEquals(personsRepository.findAll(findAllQuery).collect(toSet()), newHashSet(person1, person2, person3));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(HEIGHT, 180).or().eq(HEIGHT, 177);
		assertEquals(personsRepository.count(countQuery), 2);
	}

	@Override
	protected void testDecimal()
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(ACCOUNT_BALANCE, 299.99).or().eq(ACCOUNT_BALANCE,
				43.21);
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(ACCOUNT_BALANCE, 299.99).or()
				.eq(ACCOUNT_BALANCE, 1000.00).or().eq(ACCOUNT_BALANCE, -0.70);
		assertEquals(personsRepository.findAll(findAllQuery).collect(toSet()), newHashSet(person1, person2, person3));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(ACCOUNT_BALANCE, 299.99).or().eq(ACCOUNT_BALANCE, 1000);
		assertEquals(personsRepository.count(countQuery), 2);
	}

	@Override
	protected void testLong()
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(SERIAL_NUMBER, 374278348334L).or().eq(SERIAL_NUMBER,
				376578342134L);
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(SERIAL_NUMBER, 374278348334L).or()
				.eq(SERIAL_NUMBER, 67986789879L).or().eq(SERIAL_NUMBER, 23471900909L);
		assertEquals(personsRepository.findAll(findAllQuery).collect(toSet()), newHashSet(person1, person2, person3));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(SERIAL_NUMBER, 67986789879L).or().eq(SERIAL_NUMBER,
				23471900909L);
		assertEquals(personsRepository.count(countQuery), 2);
	}

	@Override
	protected void testString()
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(LAST_NAME, "doe").or().eq(FIRST_NAME, "john");
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(LAST_NAME, "duck").or().eq(FIRST_NAME, "john").or()
				.eq(FIRST_NAME, "jane");
		assertEquals(personsRepository.findAll(findAllQuery).collect(toSet()), newHashSet(person1, person2, person3));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(FIRST_NAME, "donald").or().eq(LAST_NAME, "doe");
		assertEquals(personsRepository.count(countQuery), 3);
	}

	@Override
	protected void testDate() throws ParseException
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(BIRTHDAY, dateFormat.parse("1980-06-07")).or()
				.eq(BIRTHDAY, dateFormat.parse("1976-06-07"));
		assertTrue(newHashSet(person1, person2).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(BIRTHDAY, dateFormat.parse("1980-06-07")).or()
				.eq(BIRTHDAY, dateFormat.parse("1976-06-07")).or().eq(BIRTHDAY, dateFormat.parse("1950-01-31"));
		assertEquals(personsRepository.findAll(findAllQuery).collect(toSet()), newHashSet(person1, person2, person3));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(BIRTHDAY, dateFormat.parse("1980-06-07")).or()
				.eq(BIRTHDAY, dateFormat.parse("1976-06-07"));
		assertEquals(personsRepository.count(countQuery), 2);
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 06:06:06"))
				.or().eq(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 07:07:07"));
		assertTrue(newHashSet(person1, person2).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 06:06:06"))
				.or().eq(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 07:07:07")).or()
				.eq(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 08:08:08"));
		assertEquals(personsRepository.findAll(findAllQuery).collect(toSet()), newHashSet(person1, person2, person3));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 06:06:06"))
				.or().eq(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 07:07:07"));
		assertEquals(personsRepository.count(countQuery), 2);
	}

	@Override
	protected void testBool()
	{
		Query<Entity> query = new QueryImpl<Entity>().eq(ACTIVE, true).or().eq(ACTIVE, false);
		assertTrue(newHashSet(person1, person2, person3).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(query), 3);

	}

	@Override
	protected void testMref()
	{
		Query<Entity> query = new QueryImpl<Entity>().eq(AUTHOR_OF, "MOLGENIS for dummies").or().eq(AUTHOR_OF,
				"Your database at the push of a button");
		assertTrue(newHashSet(person1, person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person1, person2));
		assertEquals(personsRepository.count(query), 2);
	}

	@Override
	protected void testXref()
	{
		Query<Entity> query = new QueryImpl<Entity>().eq(COUNTRY, "NL").or().eq(COUNTRY, "US");
		assertTrue(newHashSet(person1, person2, person3).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(query), 3);
	}
}
