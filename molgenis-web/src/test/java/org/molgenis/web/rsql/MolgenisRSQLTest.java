package org.molgenis.web.rsql;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class MolgenisRSQLTest extends AbstractMockitoTest
{
	private MolgenisRSQL molgenisRSQL;
	@Mock
	private EntityType entityType;
	@Mock
	private EntityType genderEntityType;

	public MolgenisRSQLTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		molgenisRSQL = new MolgenisRSQL(new RSQLParser());
		when(entityType.getId()).thenReturn("Person").getMock();
		Attribute nameAttr = when(mock(Attribute.class).getName()).thenReturn("name").getMock();
		when(nameAttr.getDataType()).thenReturn(STRING);
		Attribute ageAttr = when(mock(Attribute.class).getName()).thenReturn("age").getMock();
		when(ageAttr.getDataType()).thenReturn(INT);
		when(entityType.getAttribute("name")).thenReturn(nameAttr);
		when(entityType.getAttribute("age")).thenReturn(ageAttr);

		when(genderEntityType.getId()).thenReturn("Gender").getMock();
		Attribute genderIdAttribute = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		when(genderIdAttribute.getDataType()).thenReturn(INT);
		when(genderEntityType.getIdAttribute()).thenReturn(genderIdAttribute);
		Attribute genderAttr = when(mock(Attribute.class).getName()).thenReturn("gender").getMock();
		when(genderAttr.getDataType()).thenReturn(XREF);
		when(genderAttr.getRefEntity()).thenReturn(genderEntityType);
		when(entityType.getAttribute("gender")).thenReturn(genderAttr);

	}

	@Test
	public void testEquals() throws RSQLParserException
	{
		Query<Entity> q = molgenisRSQL.createQuery("name==piet", entityType);
		assertEquals(q, new QueryImpl<>().eq("name", "piet"));

		q = molgenisRSQL.createQuery("name=='piet paulusma'", entityType);
		assertEquals(q, new QueryImpl<>().eq("name", "piet paulusma"));

		q = molgenisRSQL.createQuery("age==87", entityType);
		assertEquals(q, new QueryImpl<>().eq("age", 87));
	}

	@Test(expectedExceptions = UnknownAttributeException.class)
	public void testUnknowAttribute() throws RSQLParserException
	{
		molgenisRSQL.createQuery("nonexistingattribute==piet", entityType);
	}

	@Test
	public void testGreaterThanOrEqual() throws RSQLParserException
	{
		Query<Entity> q = molgenisRSQL.createQuery("age>=87", entityType);
		assertEquals(q, new QueryImpl<>().ge("age", 87));
	}

	@Test
	public void testGreaterThan() throws RSQLParserException
	{
		Query<Entity> q = molgenisRSQL.createQuery("age>87", entityType);
		assertEquals(q, new QueryImpl<>().gt("age", 87));
	}

	@Test
	public void testLessThanOrEqual() throws RSQLParserException
	{
		Query<Entity> q = molgenisRSQL.createQuery("age<=87", entityType);
		assertEquals(q, new QueryImpl<>().le("age", 87));
	}

	@Test
	public void testLessThan() throws RSQLParserException
	{
		Query<Entity> q = molgenisRSQL.createQuery("age<87", entityType);
		assertEquals(q, new QueryImpl<>().lt("age", 87));
	}

	@Test
	public void testAnd() throws RSQLParserException
	{
		// ';' and 'and' or synonyms

		Query<Entity> q = molgenisRSQL.createQuery("name==piet and age==87", entityType);
		assertEquals(q, new QueryImpl<>().nest().eq("name", "piet").and().eq("age", 87).unnest());

		q = molgenisRSQL.createQuery("name==piet;age==87", entityType);
		assertEquals(q, new QueryImpl<>().nest().eq("name", "piet").and().eq("age", 87).unnest());
	}

	@Test
	public void testOr() throws RSQLParserException
	{
		// ',' and 'or' or synonyms

		Query<Entity> q = molgenisRSQL.createQuery("name==piet or age==87", entityType);
		assertEquals(q, new QueryImpl<>().nest().eq("name", "piet").or().eq("age", 87).unnest());

		q = molgenisRSQL.createQuery("name==piet,age==87", entityType);
		assertEquals(q, new QueryImpl<>().nest().eq("name", "piet").or().eq("age", 87).unnest());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testGreaterThanOnNonNumericalAttribute() throws RSQLParserException
	{
		molgenisRSQL.createQuery("name>87", entityType);
	}

	@Test(expectedExceptions = NumberFormatException.class)
	public void testGreaterThanWithNonNumericalArg() throws RSQLParserException
	{
		molgenisRSQL.createQuery("age>bogus", entityType);
	}

	@Test
	public void testComplexQuery() throws RSQLParserException
	{
		Query<Entity> q = molgenisRSQL.createQuery("((name==piet;age==87),(name==klaas;age>100))", entityType);
		assertEquals(q, new QueryImpl<>().nest()
										 .nest()
										 .eq("name", "piet")
										 .and()
										 .eq("age", 87)
										 .unnest()
										 .or()
										 .nest()
										 .eq("name", "klaas")
										 .and()
										 .gt("age", 100)
										 .unnest()
										 .unnest());
	}

	@Test
	public void testXrefIntegerIdValue()
	{
		Query<Entity> q = molgenisRSQL.createQuery("gender==2", entityType);
		assertEquals(q, new QueryImpl<>().eq("gender", 2));
	}
}
