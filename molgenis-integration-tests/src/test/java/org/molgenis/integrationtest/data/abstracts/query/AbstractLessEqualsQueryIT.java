package org.molgenis.integrationtest.data.abstracts.query;

import static java.util.stream.Collectors.toSet;
import static org.elasticsearch.common.collect.Sets.newHashSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class AbstractLessEqualsQueryIT extends AbstractQueryIT
{
	@Override
	protected void testInt()
	{
		assertTrue(newHashSet(person1, person2, person3)
				.contains(personsRepository.findOne(lessEqualThenQueryBuilder(HEIGHT, 180))));
		assertEquals(personsRepository.findAll(lessEqualThenQueryBuilder(HEIGHT, 165)).collect(toSet()),
				newHashSet(person2));
		assertEquals(personsRepository.count(lessEqualThenQueryBuilder(HEIGHT, 190)), 3);

	}

	@Override
	protected void testDecimal()
	{
		assertTrue(newHashSet(person1, person2)
				.contains(personsRepository.findOne(lessEqualThenQueryBuilder(ACCOUNT_BALANCE, 299.99))));
		assertEquals(personsRepository.findAll(lessEqualThenQueryBuilder(ACCOUNT_BALANCE, -0.70)).collect(toSet()),
				newHashSet(person2));
		assertEquals(personsRepository.count(lessEqualThenQueryBuilder(ACCOUNT_BALANCE, 1000.00)), 3);
	}

	@Override
	protected void testLong()
	{
		assertTrue(newHashSet(person2, person3)
				.contains(personsRepository.findOne(lessEqualThenQueryBuilder(SERIAL_NUMBER, 67986789879L))));
		assertEquals(personsRepository.findAll(lessEqualThenQueryBuilder(SERIAL_NUMBER, 67986789879L)).collect(toSet()),
				newHashSet(person2, person3));
		assertEquals(personsRepository.count(lessEqualThenQueryBuilder(SERIAL_NUMBER, 67986789879L)), 2);
	}

	@Override
	protected void testString()
	{
		// NOT SUPPORTED
	}

	@Override
	protected void testDate() throws ParseException
	{
		assertTrue(newHashSet(person1, person3).contains(
				personsRepository.findOne(lessEqualThenQueryBuilder(BIRTHDAY, dateFormat.parse("1978-12-12")))));
		assertEquals(personsRepository.findAll(lessEqualThenQueryBuilder(BIRTHDAY, dateFormat.parse("1950-12-12")))
				.collect(toSet()), newHashSet(person3));
		assertEquals(personsRepository.count(lessEqualThenQueryBuilder(BIRTHDAY, dateFormat.parse("2016-21-11"))), 3);
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		assertTrue(newHashSet(person1, person2).contains(personsRepository
				.findOne(lessEqualThenQueryBuilder(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 08:08:07")))));
		assertEquals(personsRepository
				.findAll(lessEqualThenQueryBuilder(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 07:07:07")))
				.collect(toSet()), newHashSet(person1, person2));
		assertEquals(personsRepository
				.count(lessEqualThenQueryBuilder(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 07:06:08"))), 1);
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

	private Query<Entity> lessEqualThenQueryBuilder(String fieldName, Object value)
	{
		return new QueryImpl<Entity>().le(fieldName, value);
	}
}
