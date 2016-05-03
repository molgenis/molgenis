package org.molgenis.integrationtest.data.abstracts.query;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class AbstractLikeQueryIT extends AbstractQueryIT
{
	@Override
	protected void testInt()
	{
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(likeQueryBuilder(HEIGHT, 65))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(HEIGHT, 18)).collect(toSet()),
				newHashSet(person1, person3));
		assertEquals(personsRepository.count(likeQueryBuilder(HEIGHT, 1)), 3);
	}

	@Override
	protected void testDecimal()
	{
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(likeQueryBuilder(ACCOUNT_BALANCE, 99))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(ACCOUNT_BALANCE, 0)).collect(toSet()),
				newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(likeQueryBuilder(ACCOUNT_BALANCE, 100)), 1);
	}

	@Override
	protected void testLong()
	{
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(likeQueryBuilder(SERIAL_NUMBER, 67))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(SERIAL_NUMBER, 8)).collect(toSet()),
				newHashSet(person1, person2));
		assertEquals(personsRepository.count(likeQueryBuilder(SERIAL_NUMBER, 7)), 3);
	}

	@Override
	protected void testString()
	{
		assertTrue(newHashSet(person3).contains(personsRepository.findOne(likeQueryBuilder(FIRST_NAME, "do"))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(LAST_NAME, "do")).collect(toSet()),
				newHashSet(person1, person2));
		assertEquals(personsRepository.count(likeQueryBuilder(FIRST_NAME, "o")), 2);
	}

	@Override
	protected void testDate() throws ParseException
	{
		assertTrue(newHashSet(person1, person2).contains(personsRepository.findOne(likeQueryBuilder(BIRTHDAY, "06"))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(BIRTHDAY, "19")).collect(toSet()),
				newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(likeQueryBuilder(BIRTHDAY, "80")), 1);
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(likeQueryBuilder(BIRTH_TIME, "06:06"))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(BIRTH_TIME, "07")).collect(toSet()),
				newHashSet(person1, person2, person3));
		assertEquals(personsRepository.count(likeQueryBuilder(BIRTH_TIME, "1976")), 3);
	}

	@Override
	protected void testBool()
	{
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(likeQueryBuilder(ACTIVE, "fa"))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(ACTIVE, "ue")).collect(toSet()),
				newHashSet(person1, person3));
		assertEquals(personsRepository.count(likeQueryBuilder(ACTIVE, "e")), 3);
	}

	@Override
	protected void testMref()
	{
		// FIXME Throws 'PSQLException: ERROR: column this.authorOf does not exist' error
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(likeQueryBuilder(AUTHOR_OF, "database"))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(AUTHOR_OF, "MOLGENIS")).collect(toSet()),
				newHashSet(person1, person2));
		assertEquals(personsRepository.count(likeQueryBuilder(AUTHOR_OF, "milk")), 0);
	}

	@Override
	protected void testXref()
	{
		assertTrue(newHashSet(person3).contains(personsRepository.findOne(likeQueryBuilder(COUNTRY, "N"))));
		assertEquals(personsRepository.findAll(likeQueryBuilder(COUNTRY, "S")).collect(toSet()),
				newHashSet(person1, person2));
		assertEquals(personsRepository.count(likeQueryBuilder(COUNTRY, "USA")), 0);
	}

	private Query<Entity> likeQueryBuilder(String fieldName, Object value)
	{
		return new QueryImpl<Entity>().like(fieldName, valueOf(value));
	}
}
