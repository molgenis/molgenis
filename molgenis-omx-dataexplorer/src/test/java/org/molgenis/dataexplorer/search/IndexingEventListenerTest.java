package org.molgenis.dataexplorer.search;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.search.DataSetsIndexer;
import org.molgenis.omx.search.IndexingEventListener;
import org.molgenis.util.EntityImportedEvent;
import org.springframework.context.support.StaticApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IndexingEventListenerTest
{
	private StaticApplicationContext context;
	private IndexingEventListener indexingEventListener;
	private DataSetsIndexer mockDataSetsIndexer;

	@BeforeMethod
	public void setUp()
	{
		context = new StaticApplicationContext();
		mockDataSetsIndexer = mock(DataSetsIndexer.class);
		indexingEventListener = new IndexingEventListener(mockDataSetsIndexer);
		context.addApplicationListener(indexingEventListener);
		context.refresh();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void onApplicationEventEntityImportedEventDataSet()
	{
		Integer id = 2;
		EntityImportedEvent event = new EntityImportedEvent(this, DataSet.ENTITY_NAME, id);
		context.publishEvent(event);

		verify(mockDataSetsIndexer, times(0)).indexProtocols(any(List.class));
		verify(mockDataSetsIndexer).indexDataSets(Arrays.asList(id));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void onApplicationEventEntityImportedEventProtocol()
	{
		Integer id = 2;
		EntityImportedEvent event = new EntityImportedEvent(this, Protocol.ENTITY_NAME, id);
		context.publishEvent(event);

		verify(mockDataSetsIndexer).indexProtocols(Arrays.asList(id));
		verify(mockDataSetsIndexer, times(0)).indexDataSets(any(List.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void onApplicationEventEntityImportedEventOther()
	{
		Integer id = 2;
		EntityImportedEvent event = new EntityImportedEvent(this, "entityThatDoesNotRequireIndexing", id);
		context.publishEvent(event);

		verify(mockDataSetsIndexer, times(0)).indexProtocols(any(List.class));
		verify(mockDataSetsIndexer, times(0)).indexDataSets(any(List.class));
	}
}
