package org.molgenis.elasticsearch.request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.molgenis.framework.db.QueryRule.Operator.SORTASC;
import static org.molgenis.framework.db.QueryRule.Operator.SORTDESC;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.molgenis.framework.db.QueryRule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SortGeneratorTest
{
	private SearchRequestBuilder searchRequestBuilderMock;

	@BeforeMethod
	public void beforeMethod()
	{
		searchRequestBuilderMock = mock(SearchRequestBuilder.class);
	}

	@Test
	public void testGenerateASC()
	{
		SortGenerator sortGen = new SortGenerator();
		sortGen.addQueryRule(new QueryRule(SORTASC, "test"));
		sortGen.generate(searchRequestBuilderMock);
		verify(searchRequestBuilderMock).addSort("test.sort", SortOrder.ASC);
	}

	@Test
	public void testGenerateDESC()
	{
		SortGenerator sortGen = new SortGenerator();
		sortGen.addQueryRule(new QueryRule(SORTDESC, "test"));
		sortGen.generate(searchRequestBuilderMock);
		verify(searchRequestBuilderMock).addSort("test.sort", SortOrder.DESC);
	}

	@Test
	public void testGenerateDESCASC()
	{
		SortGenerator sortGen = new SortGenerator();
		sortGen.addQueryRule(new QueryRule(SORTDESC, "test"));
		sortGen.addQueryRule(new QueryRule(SORTASC, "xxx"));
		sortGen.generate(searchRequestBuilderMock);
		verify(searchRequestBuilderMock).addSort("test.sort", SortOrder.DESC);
		verify(searchRequestBuilderMock).addSort("xxx.sort", SortOrder.ASC);
	}
}
