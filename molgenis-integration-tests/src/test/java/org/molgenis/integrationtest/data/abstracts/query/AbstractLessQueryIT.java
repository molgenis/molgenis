package org.molgenis.integrationtest.data.abstracts.query;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;

import org.elasticsearch.common.collect.Sets;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class AbstractLessQueryIT extends AbstractQueryIT
{

	@Override
	void testInt()
	{
		Query<Entity> query = new QueryImpl<>().lt(HEIGHT, 180);
		Set<Entity> resultSet = Sets.newHashSet(person2);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());

	}

	@Override
	void testDecimal()
	{
		Query<Entity> query = new QueryImpl<>().lt(ACCOUNT_BALANCE, 1000.00);
		Set<Entity> resultSet = Sets.newHashSet(person1, person2);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	void testLong()
	{
		Query<Entity> query = new QueryImpl<>().lt(SERIAL_NUMBER, 67986789879L);
		Set<Entity> resultSet = Sets.newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	void testString()
	{
		// TODO NOT SUPPORTED
	}

	@Override
	void testDate() throws ParseException
	{
		Query<Entity> query = new QueryImpl<>().lt(BIRTHDAY, dateFormat.parse("2000-06-07"));
		Set<Entity> resultSet = Sets.newHashSet(person1, person2, person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	void testDateTime() throws ParseException
	{
		 Query<Entity> query = new QueryImpl<>().lt(BIRTH_TIME, dateTimeFormat.parse("2000-06-07 08:07:06"));
		 Set<Entity> resultSet = Sets.newHashSet(person1, person2, person3);
		 assertTrue(resultSet.contains(personsRepository.findOne(query)));
		 assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		 assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	void testBool()
	{
		// TODO NOT SUPPORTED
	}

	@Override
	void testMref()
	{
		// TODO NOT SUPPORTED

	}

	@Override
	void testXref()
	{
		// TODO NOT SUPPORTED

	}

}
