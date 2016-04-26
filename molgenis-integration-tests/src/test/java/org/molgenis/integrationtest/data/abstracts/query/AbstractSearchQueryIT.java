package org.molgenis.integrationtest.data.abstracts.query;

import static org.testng.Assert.assertEquals;

import java.text.ParseException;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

public class AbstractSearchQueryIT extends AbstractQueryIT
{

	@Override
	void testInt()
	{
		Query<Entity> query = new QueryImpl<>().search(String.valueOf(18));
		assertEquals(personsRepository.findOne(query), person1);

	}

	@Override
	void testDecimal()
	{
		// TODO Auto-generated method stub

	}

	@Override
	void testLong()
	{
		// TODO Auto-generated method stub

	}

	@Override
	void testString()
	{
		// TODO Auto-generated method stub

	}

	@Override
	void testDate() throws ParseException
	{
		// TODO Auto-generated method stub

	}

	@Override
	void testDateTime() throws ParseException
	{
		// TODO Auto-generated method stub

	}

	@Override
	void testBool()
	{
		// TODO Auto-generated method stub

	}

	@Override
	void testMref()
	{
		// TODO Auto-generated method stub

	}

	@Override
	void testXref()
	{
		// TODO Auto-generated method stub

	}

}
