package org.molgenis.integrationtest.data.abstracts.query;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.testng.Assert;

import java.text.ParseException;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.*;
import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.*;

public class AbstractLikeQueryIT extends AbstractQueryIT
{
	@Override
	void testInt()
	{
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(likeQueryBuilder(HEIGHT, 65))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(HEIGHT, 18)).collect(toSet()), newHashSet(person1, person3));
		assertEquals(personsRepository.count(likeQueryBuilder(HEIGHT, 1)), 3);
	}

	@Override
	void testDecimal()
	{
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(likeQueryBuilder(ACCOUNT_BALANCE, 99))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(ACCOUNT_BALANCE, 0)).collect(toSet()), newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(likeQueryBuilder(ACCOUNT_BALANCE, 100)), 1);
	}

	@Override
	void testLong()
	{
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(likeQueryBuilder(SERIAL_NUMBER, 67))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(SERIAL_NUMBER, 8)).collect(toSet()), newHashSet(person1, person2));
		assertEquals(personsRepository.count(likeQueryBuilder(SERIAL_NUMBER, 7)), 3);
	}

	@Override
	void testString()
	{
		assertTrue(newHashSet(person3).contains(personsRepository.findOne(likeQueryBuilder(FIRST_NAME, "do"))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(LAST_NAME, "do")).collect(toSet()), newHashSet(person1, person2));
		assertEquals(personsRepository.count(likeQueryBuilder(FIRST_NAME, "o")), 2);
	}

	@Override
	void testDate() throws ParseException
	{
		assertTrue(newHashSet(person1, person2).contains(personsRepository.findOne(likeQueryBuilder(BIRTHDAY, "06"))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(BIRTHDAY, "19")).collect(toSet()), newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(likeQueryBuilder(BIRTHDAY, "80")), 1);
	}

	@Override
	void testDateTime() throws ParseException
	{
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(likeQueryBuilder(BIRTH_TIME, "06:06"))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(BIRTH_TIME, "07")).collect(toSet()), newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(likeQueryBuilder(BIRTH_TIME, "1976")), 3);
	}

	@Override
	void testBool()
	{
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(likeQueryBuilder(ACTIVE, "fa"))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(ACTIVE, "ue")).collect(toSet()), newHashSet(person1, person3));
		assertEquals(personsRepository.count(likeQueryBuilder(ACTIVE, "e")), 3);
	}

	@Override
	void testMref()
	{
		// FIXME Throws 'PSQLException: ERROR: column this.authorOf does not exist' error
//		assertTrue(newHashSet(person2).contains(personsRepository.findOne(likeQueryBuilder(AUTHOR_OF, "database"))));
//		assertEquals(personsRepository.findAll(likeQueryBuilder(AUTHOR_OF, "MOLGENIS")).collect(toSet()), newHashSet(person1, person2));
//		assertEquals(personsRepository.count(likeQueryBuilder(AUTHOR_OF, "milk")), 0);
	}

	@Override
	void testXref()
	{
		assertTrue(newHashSet(person3).contains(personsRepository.findOne(likeQueryBuilder(COUNTRY, "N"))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(COUNTRY, "S")).collect(toSet()), newHashSet(person1, person2));
		assertEquals(personsRepository.count(likeQueryBuilder(COUNTRY, "USA")), 0);
	}

	private Query<Entity> likeQueryBuilder(String fieldName, Object value)
	{
		return new QueryImpl<Entity>().like(fieldName, valueOf(value));
	}
}
