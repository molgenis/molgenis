package org.molgenis.data.rsql;

import cz.jirutka.rsql.parser.RSQLParserException;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class MolgenisRSQLTest
{
	private MolgenisRSQL molgenisRSQL;
	private EntityMetaData entityMetaData;
	private EntityMetaData genderEntityMetaData;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		molgenisRSQL = new MolgenisRSQL();
		entityMetaData = when(mock(EntityMetaData.class).getName()).thenReturn("Person").getMock();
		AttributeMetaData nameAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("name").getMock();
		when(nameAttr.getDataType()).thenReturn(STRING);
		AttributeMetaData ageAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("age").getMock();
		when(ageAttr.getDataType()).thenReturn(INT);
		when(entityMetaData.getAttribute("name")).thenReturn(nameAttr);
		when(entityMetaData.getAttribute("age")).thenReturn(ageAttr);

		genderEntityMetaData = when(mock(EntityMetaData.class).getName()).thenReturn("Gender").getMock();
		AttributeMetaData genderIdAttribute = when(mock(AttributeMetaData.class).getName()).thenReturn("id").getMock();
		when(genderIdAttribute.getDataType()).thenReturn(INT);
		when(genderEntityMetaData.getIdAttribute()).thenReturn(genderIdAttribute);
		AttributeMetaData genderAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("gender").getMock();
		when(genderAttr.getDataType()).thenReturn(XREF);
		when(genderAttr.getRefEntity()).thenReturn(genderEntityMetaData);
		when(entityMetaData.getAttribute("gender")).thenReturn(genderAttr);

	}

	@Test
	public void testEquals() throws RSQLParserException
	{
		Query<Entity> q = molgenisRSQL.createQuery("name==piet", entityMetaData);
		assertEquals(q, new QueryImpl<>().eq("name", "piet"));

		q = molgenisRSQL.createQuery("name=='piet paulusma'", entityMetaData);
		assertEquals(q, new QueryImpl<>().eq("name", "piet paulusma"));

		q = molgenisRSQL.createQuery("age==87", entityMetaData);
		assertEquals(q, new QueryImpl<>().eq("age", 87));
	}

	@Test(expectedExceptions = UnknownAttributeException.class)
	public void testUnknowAttribute() throws RSQLParserException
	{
		molgenisRSQL.createQuery("nonexistingattribute==piet", entityMetaData);
	}

	@Test
	public void testGreaterThanOrEqual() throws RSQLParserException
	{
		Query<Entity> q = molgenisRSQL.createQuery("age>=87", entityMetaData);
		assertEquals(q, new QueryImpl<>().ge("age", 87));
	}

	@Test
	public void testGreaterThan() throws RSQLParserException
	{
		Query<Entity> q = molgenisRSQL.createQuery("age>87", entityMetaData);
		assertEquals(q, new QueryImpl<>().gt("age", 87));
	}

	@Test
	public void testLessThanOrEqual() throws RSQLParserException
	{
		Query<Entity> q = molgenisRSQL.createQuery("age<=87", entityMetaData);
		assertEquals(q, new QueryImpl<>().le("age", 87));
	}

	@Test
	public void testLessThan() throws RSQLParserException
	{
		Query<Entity> q = molgenisRSQL.createQuery("age<87", entityMetaData);
		assertEquals(q, new QueryImpl<>().lt("age", 87));
	}

	@Test
	public void testAnd() throws RSQLParserException
	{
		// ';' and 'and' or synonyms

		Query<Entity> q = molgenisRSQL.createQuery("name==piet and age==87", entityMetaData);
		assertEquals(q, new QueryImpl<>().nest().eq("name", "piet").and().eq("age", 87).unnest());

		q = molgenisRSQL.createQuery("name==piet;age==87", entityMetaData);
		assertEquals(q, new QueryImpl<>().nest().eq("name", "piet").and().eq("age", 87).unnest());
	}

	@Test
	public void testOr() throws RSQLParserException
	{
		// ',' and 'or' or synonyms

		Query<Entity> q = molgenisRSQL.createQuery("name==piet or age==87", entityMetaData);
		assertEquals(q, new QueryImpl<>().nest().eq("name", "piet").or().eq("age", 87).unnest());

		q = molgenisRSQL.createQuery("name==piet,age==87", entityMetaData);
		assertEquals(q, new QueryImpl<>().nest().eq("name", "piet").or().eq("age", 87).unnest());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testGreaterThanOnNonNumericalAttribute() throws RSQLParserException
	{
		molgenisRSQL.createQuery("name>87", entityMetaData);
	}

	@Test(expectedExceptions = NumberFormatException.class)
	public void testGreaterThanWithNonNumericalArg() throws RSQLParserException
	{
		molgenisRSQL.createQuery("age>bogus", entityMetaData);
	}

	@Test
	public void testComplexQuery() throws RSQLParserException
	{
		Query<Entity> q = molgenisRSQL.createQuery("((name==piet;age==87),(name==klaas;age>100))", entityMetaData);
		assertEquals(q, new QueryImpl<>().nest().nest().eq("name", "piet").and().eq("age", 87).unnest().or().nest()
				.eq("name", "klaas").and().gt("age", 100).unnest().unnest());
	}

	@Test
	public void testXrefIntegerIdValue()
	{
		Query<Entity> q = molgenisRSQL.createQuery("gender==2", entityMetaData);
		assertEquals(q, new QueryImpl<>().eq("gender", 2));
	}
}
