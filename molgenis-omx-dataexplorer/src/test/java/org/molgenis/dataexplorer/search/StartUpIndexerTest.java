package org.molgenis.dataexplorer.search;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.util.DataSetImportedEvent;
import org.springframework.context.support.StaticApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StartUpIndexerTest
{
	private StaticApplicationContext context;
	private StartUpIndexer startUpIndexer;
	private DataSetsIndexer mockDataSetsIndexer;

	@BeforeMethod
	public void setUp()
	{
		context = new StaticApplicationContext();
		mockDataSetsIndexer = mock(DataSetsIndexer.class);
		startUpIndexer = new StartUpIndexer(mockDataSetsIndexer);
		context.addApplicationListener(startUpIndexer);
		context.refresh();
	}

	@Test
	public void onApplicationEventContextRefreshedEvent() throws DatabaseException, TableException
	{
		verify(mockDataSetsIndexer).indexNew();
	}

	@Test
	public void onApplicationEventDataSetImportedEvent() throws DatabaseException, TableException
	{
		Integer id = 2;
		DataSetImportedEvent event = new DataSetImportedEvent(this, id);
		context.publishEvent(event);

		verify(mockDataSetsIndexer).index(Arrays.asList(id));
	}

}
