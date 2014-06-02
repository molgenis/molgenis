package org.molgenis.data.rsql;

import static org.testng.Assert.assertEquals;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.springframework.core.convert.ConversionFailedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cz.jirutka.rsql.parser.RSQLParserException;

public class MolgenisRSQLTest
{
	private MolgenisRSQL molgenisRSQL;
	private DefaultEntityMetaData entityMetaData;

	@BeforeMethod
	public void beforeMethod()
	{
		molgenisRSQL = new MolgenisRSQL();
		entityMetaData = new DefaultEntityMetaData("Person");
		entityMetaData.addAttribute("name");
		entityMetaData.addAttribute("age").setDataType(MolgenisFieldTypes.INT);
	}

	@Test
	public void testEquals() throws RSQLParserException
	{
		Query q = molgenisRSQL.createQuery("name==piet", entityMetaData);
		assertEquals(q, new QueryImpl().eq("name", "piet"));

		q = molgenisRSQL.createQuery("name=='piet paulusma'", entityMetaData);
		assertEquals(q, new QueryImpl().eq("name", "piet paulusma"));

		q = molgenisRSQL.createQuery("age==87", entityMetaData);
		assertEquals(q, new QueryImpl().eq("age", 87));
	}

	@Test(expectedExceptions = UnknownAttributeException.class)
	public void testUnknowAttribute() throws RSQLParserException
	{
		molgenisRSQL.createQuery("nonexistingattribute==piet", entityMetaData);
	}

	@Test
	public void testGreaterThanOrEqual() throws RSQLParserException
	{
		Query q = molgenisRSQL.createQuery("age>=87", entityMetaData);
		assertEquals(q, new QueryImpl().ge("age", 87));
	}

	@Test
	public void testGreaterThan() throws RSQLParserException
	{
		Query q = molgenisRSQL.createQuery("age>87", entityMetaData);
		assertEquals(q, new QueryImpl().gt("age", 87));
	}

	@Test
	public void testLessThanOrEqual() throws RSQLParserException
	{
		Query q = molgenisRSQL.createQuery("age<=87", entityMetaData);
		assertEquals(q, new QueryImpl().le("age", 87));
	}

	@Test
	public void testLessThan() throws RSQLParserException
	{
		Query q = molgenisRSQL.createQuery("age<87", entityMetaData);
		assertEquals(q, new QueryImpl().lt("age", 87));
	}

	@Test
	public void testAnd() throws RSQLParserException
	{
		// ';' and 'and' or synonyms

		Query q = molgenisRSQL.createQuery("name==piet and age==87", entityMetaData);
		assertEquals(q, new QueryImpl().nest().eq("name", "piet").and().eq("age", 87).unnest());

		q = molgenisRSQL.createQuery("name==piet;age==87", entityMetaData);
		assertEquals(q, new QueryImpl().nest().eq("name", "piet").and().eq("age", 87).unnest());
	}

	@Test
	public void testOr() throws RSQLParserException
	{
		// ',' and 'or' or synonyms

		Query q = molgenisRSQL.createQuery("name==piet or age==87", entityMetaData);
		assertEquals(q, new QueryImpl().nest().eq("name", "piet").or().eq("age", 87).unnest());

		q = molgenisRSQL.createQuery("name==piet,age==87", entityMetaData);
		assertEquals(q, new QueryImpl().nest().eq("name", "piet").or().eq("age", 87).unnest());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testGreaterThanOnNonNumericalAttribute() throws RSQLParserException
	{
		molgenisRSQL.createQuery("name>87", entityMetaData);
	}

	@Test(expectedExceptions = ConversionFailedException.class)
	public void testGreaterThanWithNonNumericalArg() throws RSQLParserException
	{
		molgenisRSQL.createQuery("age>bogus", entityMetaData);
	}

	@Test
	public void testComplexQuery() throws RSQLParserException
	{
		Query q = molgenisRSQL.createQuery("((name==piet;age==87),(name==klaas;age>100))", entityMetaData);
		assertEquals(
				q,
				new QueryImpl().nest().nest().eq("name", "piet").and().eq("age", 87).unnest().or().nest()
						.eq("name", "piet").and().gt("age", 100).unnest().unnest());
	}
}
