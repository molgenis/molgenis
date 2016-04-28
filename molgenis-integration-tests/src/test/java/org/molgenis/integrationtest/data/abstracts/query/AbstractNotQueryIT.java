package org.molgenis.integrationtest.data.abstracts.query;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

import java.text.ParseException;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// FIXME The NOT operator fails in the PostgreSql query builder, builds ...NOT AND... instead of ...NOT...
public class AbstractNotQueryIT extends AbstractQueryIT
{
	@Override
	protected void testInt()
	{
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(notEqualsQueryBuilder(HEIGHT, 180))));
		assertEquals(personsRepository.findAll(notEqualsQueryBuilder(HEIGHT, 165)).collect(toSet()),
				newHashSet(person1, person3));
		assertEquals(personsRepository.count(notEqualsQueryBuilder(HEIGHT, 100)), 3);
	}

	@Override
	protected void testDecimal()
	{
		assertTrue(newHashSet(person1, person2)
				.contains(personsRepository.findOne(notEqualsQueryBuilder(ACCOUNT_BALANCE, 1000.00))));
		assertEquals(personsRepository.findAll(notEqualsQueryBuilder(ACCOUNT_BALANCE, -0.70)).collect(toSet()),
				newHashSet(person1, person3));
		assertEquals(personsRepository.count(notEqualsQueryBuilder(ACCOUNT_BALANCE, 1.23)), 3);
	}

	@Override
	protected void testLong()
	{
		assertTrue(newHashSet(person1, person2)
				.contains(personsRepository.findOne(notEqualsQueryBuilder(SERIAL_NUMBER, 23471900909L))));
		assertEquals(personsRepository.findAll(notEqualsQueryBuilder(SERIAL_NUMBER, 67986789879L)).collect(toSet()),
				newHashSet(person1, person3));
		assertEquals(personsRepository.count(notEqualsQueryBuilder(SERIAL_NUMBER, 67986789879L)), 2);
	}

	@Override
	protected void testString()
	{
		assertTrue(newHashSet(person3).contains(personsRepository.findOne(notEqualsQueryBuilder(LAST_NAME, "doe"))));
		assertEquals(personsRepository.findAll(notEqualsQueryBuilder(FIRST_NAME, "donald")).collect(toSet()),
				newHashSet(person1, person2));
		assertEquals(personsRepository.count(notEqualsQueryBuilder(LAST_NAME, "joe")), 3);
	}

	@Override
	protected void testDate() throws ParseException
	{
		assertTrue(newHashSet(person1, person2)
				.contains(personsRepository.findOne(notEqualsQueryBuilder(BIRTHDAY, dateFormat.parse("1950-01-31")))));
		assertEquals(personsRepository.findAll(notEqualsQueryBuilder(BIRTHDAY, dateFormat.parse("1950-01-31")))
				.collect(toSet()), newHashSet(person1, person2));
		assertEquals(personsRepository.count(notEqualsQueryBuilder(BIRTHDAY, dateFormat.parse("1950-01-31"))), 2);
	}

	@Override
	protected void testDateTime() throws ParseException
	{
		assertTrue(newHashSet(person1, person2).contains(personsRepository
				.findOne(notEqualsQueryBuilder(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 08:08:08")))));
		assertEquals(
				personsRepository.findAll(notEqualsQueryBuilder(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 08:08:08")))
						.collect(toSet()), newHashSet(person1, person2));
		assertEquals(
				personsRepository.count(notEqualsQueryBuilder(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 08:08:08"))),
				2);
	}

	@Override
	protected void testBool()
	{
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(notEqualsQueryBuilder(ACTIVE, true))));
		assertEquals(personsRepository.findAll(notEqualsQueryBuilder(ACTIVE, false)).collect(toSet()),
				newHashSet(person1, person3));
		assertEquals(personsRepository.count(notEqualsQueryBuilder(ACTIVE, true)), 1);
	}

	@Override
	protected void testMref()
	{
		assertTrue(newHashSet(person3, person2)
				.contains(personsRepository.findOne(notEqualsQueryBuilder(AUTHOR_OF, "MOLGENIS for dummies"))));

		// FIXME PostgreSql Repository finds person1 and person2, while the query is NOT book2 (which is owned by person2)
//		assertEquals(
//				personsRepository.findAll(notEqualsQueryBuilder(AUTHOR_OF, "Your database at the push of a button"))
//						.collect(toSet()), newHashSet(person1, person3));
		assertEquals(personsRepository.count(notEqualsQueryBuilder(AUTHOR_OF, "MOLGENIS for dummies")), 1);
	}

	@Override
	protected void testXref()
	{
		assertTrue(newHashSet(person3).contains(personsRepository.findOne(notEqualsQueryBuilder(COUNTRY, "US"))));
		assertEquals(personsRepository.findAll(notEqualsQueryBuilder(COUNTRY, "NL")).collect(toSet()),
				newHashSet(person1, person2));
		assertEquals(personsRepository.count(notEqualsQueryBuilder(COUNTRY, "DE")), 3);
	}

	private Query<Entity> notEqualsQueryBuilder(String fieldName, Object value)
	{
		return new QueryImpl<Entity>().not().eq(fieldName, value);
	}
}