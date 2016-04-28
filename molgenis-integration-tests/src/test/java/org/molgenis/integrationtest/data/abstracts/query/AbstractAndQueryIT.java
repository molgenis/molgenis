package org.molgenis.integrationtest.data.abstracts.query;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

import java.text.ParseException;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.*;
import static java.util.Collections.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AbstractAndQueryIT extends AbstractQueryIT
{
	@Override
	void testInt()
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(HEIGHT, 180).and().eq(FIRST_NAME, "john");
		assertTrue(newHashSet(person1).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(HEIGHT, 165).and().eq(LAST_NAME, "doe");
		assertEquals(personsRepository.findAll(findAllQuery).collect(Collectors.toSet()), newHashSet(person2));

		Query<Entity> countQuery = new QueryImpl<Entity>().eq(HEIGHT, 180).and().eq(AUTHOR_OF, emptyList());
		assertEquals(personsRepository.count(countQuery), 1);
	}

	@Override
	void testDecimal()
	{

	}

	@Override
	void testLong()
	{

	}

	@Override
	void testString()
	{

	}

	@Override
	void testDate() throws ParseException
	{

	}

	@Override
	void testDateTime() throws ParseException
	{

	}

	@Override
	void testBool()
	{

	}

	@Override
	void testMref()
	{

	}

	@Override
	void testXref()
	{

	}
}
