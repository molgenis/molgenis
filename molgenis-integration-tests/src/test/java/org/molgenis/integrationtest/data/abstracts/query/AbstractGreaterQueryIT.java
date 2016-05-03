package org.molgenis.integrationtest.data.abstracts.query;

import static java.util.stream.Collectors.toSet;
import static org.elasticsearch.common.collect.Sets.newHashSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class AbstractGreaterQueryIT extends AbstractQueryIT
{
	@Override
	protected void testInt()
	{
		assertTrue(
				newHashSet(person1, person3).contains(personsRepository.findOne(greaterThenQueryBuilder(HEIGHT, 170))));
		assertEquals(personsRepository.findAll(greaterThenQueryBuilder(HEIGHT, 164)).collect(toSet()),
				newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(greaterThenQueryBuilder(HEIGHT, 190)), 0);
	}

	@Override
	protected void testDecimal()
	{
		assertTrue(
				newHashSet(person3).contains(personsRepository.findOne(greaterThenQueryBuilder(ACCOUNT_BALANCE, 300))));
		assertEquals(personsRepository.findAll(greaterThenQueryBuilder(ACCOUNT_BALANCE, -1.00)).collect(toSet()),
				newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(greaterThenQueryBuilder(ACCOUNT_BALANCE, 154.21)), 2);
	}

	@Override
	protected void testLong()
	{
		assertTrue(newHashSet(person1)
				.contains(personsRepository.findOne(greaterThenQueryBuilder(SERIAL_NUMBER, 67986789879L))));
		assertEquals(personsRepository.findAll(greaterThenQueryBuilder(SERIAL_NUMBER, 23471900909L)).collect(toSet()),
				newHashSet(person1, person2));
		assertEquals(personsRepository.count(greaterThenQueryBuilder(SERIAL_NUMBER, 67986789878L)), 2);
	}

	@Override
	protected void testString()
	{
		// TODO NOT SUPPORTED
	}

	@Override
	protected void testDate() throws ParseException
	{
		assertTrue(newHashSet(person2).contains(
				personsRepository.findOne(greaterThenQueryBuilder(BIRTHDAY, dateFormat.parse("1978-12-12")))));
		assertEquals(personsRepository.findAll(greaterThenQueryBuilder(BIRTHDAY, dateFormat.parse("1942-23-01")))
				.collect(toSet()), newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(greaterThenQueryBuilder(BIRTHDAY, dateFormat.parse("2016-21-11"))), 0);
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		assertTrue(newHashSet(person3).contains(personsRepository
				.findOne(greaterThenQueryBuilder(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 08:08:07")))));
		assertEquals(personsRepository
				.findAll(greaterThenQueryBuilder(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 03:08:08")))
				.collect(toSet()), newHashSet(person1, person2, person3));
		assertEquals(personsRepository
				.count(greaterThenQueryBuilder(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 07:06:08"))), 2);
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

	private Query<Entity> greaterThenQueryBuilder(String fieldName, Object value)
	{
		return new QueryImpl<Entity>().gt(fieldName, value);
	}
}
