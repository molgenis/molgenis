package org.molgenis.data.elasticsearch.request;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.molgenis.data.Entity;
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
		gen.generate(searchRequestBuilderMock, new QueryImpl<Entity>().pageSize(20), null);
		verify(searchRequestBuilderMock).setSize(20);
	}

	@Test
	public void testGenerateSizeUndefined()
	{
		LimitOffsetGenerator gen = new LimitOffsetGenerator();
		gen.generate(searchRequestBuilderMock, new QueryImpl<>(), null);
		verify(searchRequestBuilderMock, times(0)).setSize(any(Integer.class));
	}

	@Test
	public void testGenerateFrom()
	{
		LimitOffsetGenerator gen = new LimitOffsetGenerator();
		gen.generate(searchRequestBuilderMock, new QueryImpl<Entity>().offset(20), null);
		verify(searchRequestBuilderMock).setFrom(20);
	}

	@Test
	public void testGenerateFromUndefined()
	{
		LimitOffsetGenerator gen = new LimitOffsetGenerator();
		gen.generate(searchRequestBuilderMock, new QueryImpl<>(), null);
		verify(searchRequestBuilderMock).setFrom(0);
	}

}
