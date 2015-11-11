package org.molgenis.data.elasticsearch.request;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SourceFilteringGeneratorTest
{
	private SearchRequestBuilder searchRequestBuilder;
	private EntityMetaData entityMeta;

	@BeforeMethod
	public void beforeMethod()
	{
		searchRequestBuilder = mock(SearchRequestBuilder.class);
		entityMeta = mock(EntityMetaData.class);
	}

	@Test
	public void generateNoElasticsearchBackend()
	{
		when(entityMeta.getBackend()).thenReturn("notElasticsearch");
		Query q = new QueryImpl().fetch(new Fetch().field("field0"));
		new SourceFilteringGenerator().generate(searchRequestBuilder, q, entityMeta);
		verifyNoMoreInteractions(searchRequestBuilder);
	}

	@Test
	public void generateElasticsearchBackendNoFetch()
	{
		when(entityMeta.getBackend()).thenReturn("notElasticsearch");
		Query q = new QueryImpl();
		new SourceFilteringGenerator().generate(searchRequestBuilder, q, entityMeta);
		verifyNoMoreInteractions(searchRequestBuilder);
	}

	@Test
	public void generateElasticsearchBackendFetchNoSubFetch()
	{
		when(entityMeta.getBackend()).thenReturn(ElasticsearchRepositoryCollection.NAME);
		String attr0Name = "attr0";
		String attr1Name = "attr1";
		Query q = new QueryImpl().fetch(new Fetch().field(attr0Name).field(attr1Name));
		new SourceFilteringGenerator().generate(searchRequestBuilder, q, entityMeta);
		verify(searchRequestBuilder, times(1)).setFetchSource(new String[]
		{ attr0Name, attr1Name }, null);
	}

	@Test
	public void generateElasticsearchBackendFetchSubFetch()
	{
		String attr0Name = "attr0";
		String attr1Name = "attr1";
		String refAttr0Name = "refAttr0";
		when(entityMeta.getBackend()).thenReturn(ElasticsearchRepositoryCollection.NAME);
		Query q = new QueryImpl().fetch(new Fetch().field(attr0Name).field(attr1Name, new Fetch().field(refAttr0Name)));
		new SourceFilteringGenerator().generate(searchRequestBuilder, q, entityMeta);
		verify(searchRequestBuilder, times(1)).setFetchSource(new String[]
		{ attr0Name, attr1Name + '.' + refAttr0Name }, null);
	}

	@Test
	public void generateElasticsearchBackendFetchSubFetchWithSubFetch()
	{
		String attr0Name = "attr0";
		String attr1Name = "attr1";
		String refAttr0Name = "refAttr0";
		String refRefAttr0Name = "refRefAttr0";
		when(entityMeta.getBackend()).thenReturn(ElasticsearchRepositoryCollection.NAME);
		Query q = new QueryImpl().fetch(new Fetch().field(attr0Name).field(attr1Name,
				new Fetch().field(refAttr0Name, new Fetch().field(refRefAttr0Name))));
		new SourceFilteringGenerator().generate(searchRequestBuilder, q, entityMeta);

		// same as fetch with sub fetch, we ignore sub fetches of sub fetches
		verify(searchRequestBuilder, times(1)).setFetchSource(new String[]
		{ attr0Name, attr1Name + '.' + refAttr0Name }, null);
	}
}
