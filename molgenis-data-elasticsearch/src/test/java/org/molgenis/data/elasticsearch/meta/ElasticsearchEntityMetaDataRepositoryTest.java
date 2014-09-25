package org.molgenis.data.elasticsearch.meta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.ElasticSearchService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.EntityMetaDataRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ElasticsearchEntityMetaDataRepositoryTest
{
	private ElasticsearchEntityMetaDataRepository elasticsearchEntityMetaDataRepository;
	private EntityMetaDataRepository entityMetaDataRepository;
	private SearchService elasticSearchService;

	@BeforeMethod
	public void setUp()
	{
		entityMetaDataRepository = mock(EntityMetaDataRepository.class);
		elasticSearchService = mock(ElasticSearchService.class);
		elasticsearchEntityMetaDataRepository = new ElasticsearchEntityMetaDataRepository(entityMetaDataRepository,
				elasticSearchService);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ElasticsearchEntityMetaDataRepository()
	{
		new ElasticsearchEntityMetaDataRepository(null, null);
	}

	@Test
	public void addEntityMetaData()
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		elasticsearchEntityMetaDataRepository.addEntityMetaData(entityMetaData);
		verify(entityMetaDataRepository).addEntityMetaData(entityMetaData);
	}

	@Test
	public void getEntityMetaData()
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		String entityName = "entity";
		when(entityMetaDataRepository.getEntityMetaData(entityName)).thenReturn(entityMetaData);
		assertEquals(elasticsearchEntityMetaDataRepository.getEntityMetaData(entityName), entityMetaData);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getEntityMetaDatas()
	{
		Iterable<EntityMetaData> entityMetaDatas = (Iterable<EntityMetaData>) mock(Iterable.class);
		when(entityMetaDataRepository.getEntityMetaDatas()).thenReturn(entityMetaDatas);
		assertEquals(elasticsearchEntityMetaDataRepository.getEntityMetaDatas(), entityMetaDatas);
	}
}
