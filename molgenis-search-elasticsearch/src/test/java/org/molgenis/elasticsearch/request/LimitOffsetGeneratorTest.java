package org.molgenis.elasticsearch.request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LimitOffsetGeneratorTest
{
	private SearchRequestBuilder searchRequestBuilderMock;

	@BeforeMethod
	public void beforeMethod()
	{
		searchRequestBuilderMock = mock(SearchRequestBuilder.class);
	}

	@Test
	public void testGenerateSize()
	{
		LimitOffsetGenerator gen = new LimitOffsetGenerator();
		gen.generate(searchRequestBuilderMock, new QueryImpl().pageSize(20));
		verify(searchRequestBuilderMock).setSize(20);
	}

	@Test
	public void testGenerateFrom()
	{
		LimitOffsetGenerator gen = new LimitOffsetGenerator();
		gen.generate(searchRequestBuilderMock, new QueryImpl().offset(20));
		verify(searchRequestBuilderMock).setFrom(20);
	}
}
