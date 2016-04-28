package org.molgenis.integrationtest.data.abstracts.query;

import static com.google.common.collect.Lists.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.stream.Collectors;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

import com.google.common.collect.Sets;

public abstract class AbstractEqualsQueryIT extends AbstractQueryIT
{
	@Override
	protected void testInt()
	{
		Query<Entity> query = new QueryImpl<>().eq(HEIGHT, 180);
		assertTrue(Sets.newHashSet(person1, person3).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1, person3));
		assertEquals(personsRepository.count(query), 2);
	}

	@Override
	protected void testString()
	{
		Query<Entity> query = new QueryImpl<>().eq(LAST_NAME, "doe");
		assertTrue(Sets.newHashSet(person1, person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1, person2));
		assertEquals(personsRepository.count(query), 2);
	}

	@Override
	protected void testXref()
	{
		Query<Entity> query = new QueryImpl<>().eq(COUNTRY, "US");
		assertTrue(Sets.newHashSet(person1, person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1, person2));
		assertEquals(personsRepository.count(query), 2);
	}

	@Override
	protected void testDecimal()
	{
		Query<Entity> query = new QueryImpl<>().eq(ACCOUNT_BALANCE, 1000.00);
		assertEquals(personsRepository.findOne(query), person3);
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person3));
		assertEquals(personsRepository.count(query), 1);
	}

	@Override
	protected void testLong()
	{
		Query<Entity> query = new QueryImpl<>().eq(SERIAL_NUMBER, 67986789879L);
		assertEquals(personsRepository.findOne(query), person2);
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person2));
		assertEquals(personsRepository.count(query), 1);
	}

	@Override
	protected void testDate() throws ParseException
	{
		Query<Entity> query = new QueryImpl<>().eq(BIRTHDAY, dateFormat.parse("1976-06-07"));
		assertEquals(personsRepository.findOne(query), person1);
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1));
		assertEquals(personsRepository.count(query), 1);
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		Query<Entity> query = new QueryImpl<>().eq(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 07:07:07"));
		assertEquals(personsRepository.findOne(query), person2);
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person2));
		assertEquals(personsRepository.count(query), 1);
	}

	@Override
	protected void testBool()
	{
		Query<Entity> query = new QueryImpl<>().eq(ACTIVE, true);
		assertTrue(Sets.newHashSet(person1, person3).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1, person3));
		assertEquals(personsRepository.count(query), 2);
	}

	@Override
	protected void testMref()
	{
		Query<Entity> query = new QueryImpl<>().eq(AUTHOR_OF, "MOLGENIS for dummies");
		assertTrue(Sets.newHashSet(person1, person2).contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person1, person2));
		assertEquals(personsRepository.count(query), 2);

		// FIXME Works for ElasticSearch repository, it does not work for the PostgreSql repository...
//		query = new QueryImpl<>().eq(AUTHOR_OF, newArrayList("MOLGENIS for dummies", "Your database at the push of a button"));
//		assertTrue(Sets.newHashSet(person2).contains(personsRepository.findOne(query)));
//		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), Sets.newHashSet(person2));
//		assertEquals(personsRepository.count(query), 1);
	}
}
