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
import org.molgenis.data.meta.WritableMetaDataService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IndexingMetaDataRepositoriesDecoratorTest
{
	private IndexingWritableMetaDataServiceDecorator decorator;
	private WritableMetaDataService metaDataRepositories;
	private DataService dataService;
	private ElasticSearchService elasticSearchService;

	@BeforeMethod
	public void setUp()
	{
		metaDataRepositories = mock(WritableMetaDataService.class);
		dataService = mock(DataService.class);
		elasticSearchService = mock(ElasticSearchService.class);
		decorator = new IndexingWritableMetaDataServiceDecorator(metaDataRepositories, dataService,
				elasticSearchService);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ElasticsearchAttributeMetaDataRepository()
	{
		new IndexingWritableMetaDataServiceDecorator(null, null, null);
	}

	@Test
	public void addEntityMetaData()
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		decorator.addEntityMetaData(entityMetaData);
		verify(metaDataRepositories).addEntityMetaData(entityMetaData);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getEntityMetaDatas()
	{
		Iterable<EntityMetaData> entityMetaDatas = mock(Iterable.class);
		when(metaDataRepositories.getEntityMetaDatas()).thenReturn(entityMetaDatas);
		assertEquals(decorator.getEntityMetaDatas(), entityMetaDatas);
	}

	@Test
	public void addAttributeMetaData() throws IOException
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		String entityName = "entity";
		when(dataService.getEntityMetaData(entityName)).thenReturn(entityMetaData);

		AttributeMetaData attribute = mock(AttributeMetaData.class);
		decorator.addAttributeMetaData(entityName, attribute);
		verify(metaDataRepositories).addAttributeMetaData(entityName, attribute);
		verify(elasticSearchService).createMappings(entityMetaData);
	}

	@Test
	public void removeAttributeMetaData() throws IOException
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		String entityName = "entity";
		String attributeName = "attribute";
		when(dataService.getEntityMetaData(entityName)).thenReturn(entityMetaData);
		decorator.removeAttributeMetaData(entityName, attributeName);
		verify(metaDataRepositories).removeAttributeMetaData(entityName, attributeName);
		verify(elasticSearchService).createMappings(entityMetaData);
	}
}
