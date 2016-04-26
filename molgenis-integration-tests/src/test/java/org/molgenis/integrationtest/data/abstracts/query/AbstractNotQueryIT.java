package org.molgenis.integrationtest.data.abstracts.query;

import com.google.common.collect.Sets;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

import java.text.ParseException;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AbstractNotQueryIT extends AbstractQueryIT
{
	@Override
	protected void testInt()
	{
		Query findOneQuery = new QueryImpl().not().eq(HEIGHT, 180);
		assertTrue(Sets.newHashSet(person2).contains(personsRepository.findOne(findOneQuery)));

		Query<Entity> findAllQuery = new QueryImpl<Entity>().not().eq(HEIGHT, 165);
		assertEquals(personsRepository.findAll(findAllQuery).collect(Collectors.toSet()), Sets.newHashSet(person1, person3));

		Query<Entity> countQuery = new QueryImpl<Entity>().not().eq(HEIGHT, 100);
		assertEquals(personsRepository.count(countQuery), 3);
	}

	@Override protected void testDecimal()
	{
	}

	@Override protected void testLong()
	{

	}

	@Override protected void testString()
	{

	}

	@Override protected void testDate() throws ParseException
	{

	}

	@Override protected void testDateTime() throws ParseException
	{

	}

	@Override protected void testBool()
	{

	}

	@Override protected void testMref()
	{

	}

	@Override protected void testXref()
	{

	}
}
