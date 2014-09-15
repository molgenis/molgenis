package org.molgenis.data.elasticsearch.meta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.ElasticSearchService;
import org.molgenis.data.meta.AttributeMetaDataRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ElasticsearchAttributeMetaDataRepositoryTest
{
	private ElasticsearchAttributeMetaDataRepository elasticsearchAttributeMetaDataRepository;
	private AttributeMetaDataRepository attributeMetaDataRepository;
	private DataService dataService;
	private ElasticSearchService elasticSearchService;

	@BeforeMethod
	public void setUp()
	{
		attributeMetaDataRepository = mock(AttributeMetaDataRepository.class);
		dataService = mock(DataService.class);
		elasticSearchService = mock(ElasticSearchService.class);
		elasticsearchAttributeMetaDataRepository = new ElasticsearchAttributeMetaDataRepository(
				attributeMetaDataRepository, dataService, elasticSearchService);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ElasticsearchAttributeMetaDataRepository()
	{
		new ElasticsearchAttributeMetaDataRepository(null, null, null);
	}

	@Test
	public void addAttributeMetaData() throws IOException
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		String entityName = "entity";
		when(dataService.getEntityMetaData(entityName)).thenReturn(entityMetaData);

		AttributeMetaData attribute = mock(AttributeMetaData.class);
		elasticsearchAttributeMetaDataRepository.addAttributeMetaData(entityName, attribute);
		verify(attributeMetaDataRepository).addAttributeMetaData(entityName, attribute);
		verify(elasticSearchService).createMappings(entityMetaData);
	}

	@Test
	public void getEntityAttributeMetaData()
	{
		@SuppressWarnings("unchecked")
		Iterable<AttributeMetaData> attributes = (Iterable<AttributeMetaData>) mock(Iterable.class);
		String entityName = "entity";
		when(attributeMetaDataRepository.getEntityAttributeMetaData(entityName)).thenReturn(attributes);
		assertEquals(elasticsearchAttributeMetaDataRepository.getEntityAttributeMetaData(entityName), attributes);
	}

	@Test
	public void removeAttributeMetaData() throws IOException
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		String entityName = "entity";
		String attributeName = "attribute";
		when(dataService.getEntityMetaData(entityName)).thenReturn(entityMetaData);
		elasticsearchAttributeMetaDataRepository.removeAttributeMetaData(entityName, attributeName);
		verify(attributeMetaDataRepository).removeAttributeMetaData(entityName, attributeName);
		verify(elasticSearchService).createMappings(entityMetaData);
	}
}
