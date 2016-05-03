package org.molgenis.integrationtest.data.abstracts.query;

import static java.util.stream.Collectors.toSet;
import static org.elasticsearch.common.collect.Sets.newHashSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class AbstractGreaterEqualsQueryIT extends AbstractQueryIT
{
	@Override
	protected void testInt()
	{
		assertTrue(newHashSet(person1, person3)
				.contains(personsRepository.findOne(greaterEqualsThenQueryBuilder(HEIGHT, 180))));
		assertEquals(personsRepository.findAll(greaterEqualsThenQueryBuilder(HEIGHT, 165)).collect(toSet()),
				newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(greaterEqualsThenQueryBuilder(HEIGHT, 166)), 2);
	}

	@Override
	protected void testDecimal()
	{
		assertTrue(newHashSet(person1, person3)
				.contains(personsRepository.findOne(greaterEqualsThenQueryBuilder(ACCOUNT_BALANCE, 299.99))));
		assertEquals(personsRepository.findAll(greaterEqualsThenQueryBuilder(ACCOUNT_BALANCE, -0.70)).collect(toSet()),
				newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(greaterEqualsThenQueryBuilder(ACCOUNT_BALANCE, 1000.01)), 0);
	}

	@Override
	protected void testLong()
	{
		assertTrue(newHashSet(person1, person2)
				.contains(personsRepository.findOne(greaterEqualsThenQueryBuilder(SERIAL_NUMBER, 67986789879L))));
		assertEquals(
				personsRepository.findAll(greaterEqualsThenQueryBuilder(SERIAL_NUMBER, 23471900909L)).collect(toSet()),
				newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(greaterEqualsThenQueryBuilder(SERIAL_NUMBER, 67986789879L)), 2);
	}

	@Override
	protected void testString()
	{
		// NOT SUPPORTED
	}

	@Override
	protected void testDate() throws ParseException
	{
		assertTrue(newHashSet(person2).contains(
				personsRepository.findOne(greaterEqualsThenQueryBuilder(BIRTHDAY, dateFormat.parse("1978-12-12")))));
		assertEquals(personsRepository.findAll(greaterEqualsThenQueryBuilder(BIRTHDAY, dateFormat.parse("1942-23-01")))
				.collect(toSet()), newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(greaterEqualsThenQueryBuilder(BIRTHDAY, dateFormat.parse("2016-21-11"))),
				0);
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		assertTrue(newHashSet(person3).contains(personsRepository
				.findOne(greaterEqualsThenQueryBuilder(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 08:08:07")))));
		assertEquals(personsRepository
				.findAll(greaterEqualsThenQueryBuilder(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 03:08:08")))
				.collect(toSet()), newHashSet(person1, person2, person3));
		assertEquals(personsRepository
				.count(greaterEqualsThenQueryBuilder(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 07:06:08"))), 2);
	}

	@Override
	protected void testBool()
	{
		// NOT SUPPORTED
	}

	@Override
	protected void testMref()
	{
		// NOT SUPPORTED

	}

	@Override
	protected void testXref()
	{
		// NOT SUPPORTED
	}

	private Query<Entity> greaterEqualsThenQueryBuilder(String fieldName, Object value)
	{
		return new QueryImpl<Entity>().ge(fieldName, value);
	}
}
