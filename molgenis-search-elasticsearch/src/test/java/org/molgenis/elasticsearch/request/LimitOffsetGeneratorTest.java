package org.molgenis.elasticsearch.request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
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
		gen.addQueryRule(new QueryRule(Operator.LIMIT, 20));
		gen.generate(searchRequestBuilderMock);
		verify(searchRequestBuilderMock).setSize(20);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testGenerateSizeWithInvalidValue()
	{
		LimitOffsetGenerator gen = new LimitOffsetGenerator();
		gen.addQueryRule(new QueryRule(Operator.LIMIT, "20"));
		gen.generate(searchRequestBuilderMock);
	}

	@Test
	public void testGenerateFrom()
	{
		LimitOffsetGenerator gen = new LimitOffsetGenerator();
		gen.addQueryRule(new QueryRule(Operator.OFFSET, 20));
		gen.generate(searchRequestBuilderMock);
		verify(searchRequestBuilderMock).setFrom(20);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testGenerateFromeWithInvalidValue()
	{
		LimitOffsetGenerator gen = new LimitOffsetGenerator();
		gen.addQueryRule(new QueryRule(Operator.OFFSET, "20"));
		gen.generate(searchRequestBuilderMock);
	}
}
