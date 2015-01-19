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
import org.molgenis.data.meta.MetaDataService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IndexingMetaDataRepositoriesDecoratorTest
{
	private IndexingWritableMetaDataServiceDecorator decorator;
	private MetaDataService metaDataService;
	private DataService dataService;
	private ElasticSearchService elasticSearchService;

	@BeforeMethod
	public void setUp()
	{
		metaDataService = mock(MetaDataService.class);
		dataService = mock(DataService.class);
		elasticSearchService = mock(ElasticSearchService.class);
		decorator = new IndexingWritableMetaDataServiceDecorator(metaDataService, elasticSearchService);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ElasticsearchAttributeMetaDataRepository()
	{
		new IndexingWritableMetaDataServiceDecorator(null, null);
	}

	@Test
	public void addEntityMetaData()
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		decorator.addEntityMeta(entityMetaData);
		verify(metaDataService).addEntityMeta(entityMetaData);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getEntityMetaDatas()
	{
		Iterable<EntityMetaData> entityMetaDatas = mock(Iterable.class);
		when(metaDataService.getEntityMetaDatas()).thenReturn(entityMetaDatas);
		assertEquals(decorator.getEntityMetaDatas(), entityMetaDatas);
	}

	@Test
	public void addAttributeMetaData() throws IOException
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		String entityName = "entity";
		when(dataService.getEntityMetaData(entityName)).thenReturn(entityMetaData);

		AttributeMetaData attribute = mock(AttributeMetaData.class);
		decorator.addAttribute(entityName, attribute);
		verify(metaDataService).addAttribute(entityName, attribute);
		verify(elasticSearchService).createMappings(entityMetaData);
	}

	@Test
	public void removeAttributeMetaData() throws IOException
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		String entityName = "entity";
		String attributeName = "attribute";
		when(dataService.getEntityMetaData(entityName)).thenReturn(entityMetaData);
		decorator.deleteAttribute(entityName, attributeName);
		verify(metaDataService).deleteAttribute(entityName, attributeName);
		verify(elasticSearchService).createMappings(entityMetaData);
	}
}
