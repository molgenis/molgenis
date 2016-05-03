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

public class AbstractRangeQueryIT extends AbstractQueryIT
{

	@Override
	protected void testInt()
	{
		// FIXME
		// Query<Entity> query = new QueryImpl<>().rng(HEIGHT, 160, 180);
		// Set<Entity> resultSet = Sets.newHashSet(person2);
		// assertTrue(resultSet.contains(personsRepository.findOne(query)));
		// assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		// assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testDecimal()
	{
		Query<Entity> query = new QueryImpl<>().rng(ACCOUNT_BALANCE, 0, 1000);
		Set<Entity> resultSet = Sets.newHashSet(person1, person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testLong()
	{
		Query<Entity> query = new QueryImpl<>().rng(SERIAL_NUMBER, 67986789879L, Long.MAX_VALUE);
		Set<Entity> resultSet = Sets.newHashSet(person1, person2);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testString()
	{
		// UNSUPPORTED
	}

	@Override
	protected void testDate() throws ParseException
	{
		Query<Entity> query = new QueryImpl<>().rng(BIRTHDAY, dateFormat.parse("1949-01-31"),
				dateFormat.parse("1977-01-31"));
		Set<Entity> resultSet = Sets.newHashSet(person1, person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		Query<Entity> query = new QueryImpl<>().rng(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 06:06:00"),
				dateTimeFormat.parse("1976-06-07 07:08:08"));
		Set<Entity> resultSet = Sets.newHashSet(person1, person2);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());

	}

	@Override
	protected void testBool()
	{
		// UNSUPPORTED
	}

	@Override
	protected void testMref()
	{
		// UNSUPPORTED
	}

	@Override
	protected void testXref()
	{
		// UNSUPPORTED
	}

}
