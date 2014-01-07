package org.molgenis.dataexplorer.search;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.search.DataSetsIndexer;
import org.molgenis.omx.search.IndexingEventListener;
import org.molgenis.util.DataSetImportedEvent;
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

	@Test
	public void onApplicationEventDataSetImportedEvent() throws DatabaseException
	{
		Integer id = 2;
		DataSetImportedEvent event = new DataSetImportedEvent(this, id);
		context.publishEvent(event);

		verify(mockDataSetsIndexer).indexDataSets(Arrays.asList(id));
	}

}
