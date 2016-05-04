package org.molgenis.integrationtest.data.abstracts.query;

import static autovalue.shaded.com.google.common.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.Set;

import org.elasticsearch.common.collect.Sets;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class AbstractInQueryIT extends AbstractQueryIT
{

	@Override
	protected void testInt()
	{
		Query<Entity> query = new QueryImpl<>().in(HEIGHT, newArrayList(180, 165, 20));
		Set<Entity> resultSet = Sets.newHashSet(person1, person2, person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testDecimal()
	{
		Query<Entity> query = new QueryImpl<>().in(ACCOUNT_BALANCE, newArrayList(1000.00, -0.70));
		Set<Entity> resultSet = Sets.newHashSet(person2, person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testLong()
	{
		Query<Entity> query = new QueryImpl<>().in(SERIAL_NUMBER, newArrayList(374278348334L, 50L));
		assertEquals(personsRepository.findOne(query), person1);
		assertEquals(personsRepository.findAll(query).collect(toList()), newArrayList(person1));
		assertEquals(personsRepository.count(query), 1);
	}

	@Override
	protected void testString()
	{
		Query<Entity> query = new QueryImpl<>().in(LAST_NAME, newArrayList("doe", "re", "mi"));
		Set<Entity> resultSet = Sets.newHashSet(person1, person2);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testDate() throws ParseException
	{
		// FIXME ElasticSearch repository throws error:
		// FIXME IllegalArgumentException[Invalid format: "1976-06-07T07:08:08.000Z" is malformed at ".000Z"
		// Query<Entity> query = new QueryImpl<>().in(BIRTHDAY,
		// Lists.newArrayList(dateFormat.parse("1980-06-07").toInstant(), dateFormat.parse("1976-06-07").toInstant()));
		// Set<Entity> resultSet = Sets.newHashSet(person1, person2);
		// assertTrue(resultSet.contains(personsRepository.findOne(query)));
		// assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		// assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		// FIXME ElasticSearch repository throws error:
		// FIXME IllegalArgumentException[Invalid format: "1976-06-07T07:08:08.000Z" is malformed at ".000Z"
		// Query<Entity> query = new QueryImpl<>().in(
		// BIRTH_TIME, Lists.newArrayList(dateTimeFormat.parse("1976-06-07 08:08:08"), dateTimeFormat.parse("1976-06-07
		// 06:06:06")));
		// Set<Entity> resultSet = Sets.newHashSet(person1, person3);
		// assertTrue(resultSet.contains(personsRepository.findOne(query)));
		// assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		// assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testBool()
	{
		Query<Entity> query = new QueryImpl<>().in(ACTIVE, newArrayList(true, false));
		Set<Entity> resultSet = Sets.newHashSet(person1, person2, person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testMref()
	{
		Query<Entity> query = new QueryImpl<>().in(AUTHOR_OF,
				newArrayList("MOLGENIS for dummies", "Your database at the push of a button"));
		Set<Entity> resultSet = Sets.newHashSet(person1, person2);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	protected void testXref()
	{
		Query<Entity> query = new QueryImpl<>().in(COUNTRY, newArrayList("NL", "DE", "XX"));
		Set<Entity> resultSet = Sets.newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

}
