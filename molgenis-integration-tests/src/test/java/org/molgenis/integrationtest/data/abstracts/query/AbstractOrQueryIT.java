package org.molgenis.integrationtest.data.abstracts.query;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

import java.text.ParseException;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AbstractOrQueryIT extends AbstractQueryIT
{
	@Override void testInt()
	{
		Query<Entity> findOneQuery = new QueryImpl<Entity>().eq(HEIGHT, 123).or().eq(HEIGHT, 165);
		assertTrue(newHashSet(person2).contains(personsRepository.findOne(findOneQuery)));
		Query<Entity> findAllQuery = new QueryImpl<Entity>().eq(HEIGHT, 180).or().eq(HEIGHT, 165);
		assertEquals(personsRepository.findAll(findAllQuery).collect(toSet()),
				newHashSet(person1, person2, person3));
		Query<Entity> countQuery = new QueryImpl<Entity>().eq(HEIGHT, 180).or().eq(HEIGHT, 177);
		assertEquals(personsRepository.count(countQuery), 2);
	}

	@Override void testDecimal()
	{

	}

	@Override void testLong()
	{

	}

	@Override void testString()
	{

	}

	@Override void testDate() throws ParseException
	{

	}

	@Override void testDateTime() throws ParseException
	{

	}

	@Override void testBool()
	{

	}

	@Override void testMref()
	{

	}

	@Override void testXref()
	{

	}
}
