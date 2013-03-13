package org.molgenis.elasticsearch.request;

import static org.molgenis.framework.db.QueryRule.Operator.AND;
import static org.molgenis.framework.db.QueryRule.Operator.SEARCH;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.testng.annotations.Test;

public class AbstractQueryPartGeneratorTest
{

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testAbstractQueryPartGeneratorNull()
	{
		new QueryPartGeneratorMock(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testAbstractQueryPartGeneratorEmptyList()
	{
		new QueryPartGeneratorMock(Collections.<Operator> emptyList());
	}

	@Test
	public void testAddQueryRule()
	{
		QueryPartGeneratorMock gen = new QueryPartGeneratorMock(Arrays.asList(Operator.SEARCH));
		assertNotNull(gen.queryRules);
		assertTrue(gen.queryRules.isEmpty());

		QueryRule qr = new QueryRule(SEARCH, "test");
		gen.addQueryRule(qr);
		assertNotNull(gen.queryRules);
		assertEquals(gen.queryRules.size(), 1);
		assertEquals(gen.queryRules.get(0), qr);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testAddUnsupportedQueryRule()
	{
		QueryPartGeneratorMock gen = new QueryPartGeneratorMock(Arrays.asList(Operator.SEARCH));
		gen.addQueryRule(new QueryRule(AND));
	}

	@Test
	public void testSupportsOperator()
	{
		QueryPartGeneratorMock gen = new QueryPartGeneratorMock(Arrays.asList(SEARCH));
		assertTrue(gen.supportsOperator(SEARCH));
		assertFalse(gen.supportsOperator(AND));
	}

	private class QueryPartGeneratorMock extends AbstractQueryRulePartGenerator
	{

		public QueryPartGeneratorMock(List<Operator> supportedOperators)
		{
			super(supportedOperators);
		}

		@Override
		public void generate(SearchRequestBuilder searchRequestBuilder)
		{
		}

	}
}
