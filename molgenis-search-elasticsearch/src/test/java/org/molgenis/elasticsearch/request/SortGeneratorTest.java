package org.molgenis.elasticsearch.request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.molgenis.data.support.QueryImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
		sortGen.generate(searchRequestBuilderMock, new QueryImpl().sort(new Sort(Direction.ASC, "test")));
		verify(searchRequestBuilderMock).addSort("test.sort", SortOrder.ASC);
	}

	@Test
	public void testGenerateDESC()
	{
		SortGenerator sortGen = new SortGenerator();
		sortGen.generate(searchRequestBuilderMock, new QueryImpl().sort(new Sort(Direction.DESC, "test")));
		verify(searchRequestBuilderMock).addSort("test.sort", SortOrder.DESC);
	}

	@Test
	public void testGenerateDESCASC()
	{
		SortGenerator sortGen = new SortGenerator();
		sortGen.generate(searchRequestBuilderMock,
				new QueryImpl().sort(new Sort(Direction.DESC, "test").and(new Sort(Direction.ASC, "xxx"))));
		verify(searchRequestBuilderMock).addSort("test.sort", SortOrder.DESC);
		verify(searchRequestBuilderMock).addSort("xxx.sort", SortOrder.ASC);
	}
}
