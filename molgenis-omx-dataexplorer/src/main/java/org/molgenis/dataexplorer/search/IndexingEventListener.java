package org.molgenis.dataexplorer.search;

import java.util.Arrays;

import org.molgenis.framework.tupletable.TableException;
import org.molgenis.util.DataSetImportedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Indexes not yet indexed DataSets at application startup and when a dataset is imported
 * 
 * @author erwin
 * 
 */
public class IndexingEventListener implements ApplicationListener<ApplicationEvent>
{
	private final DataSetsIndexer dataSetsIndexer;

	public IndexingEventListener(DataSetsIndexer dataSetsIndexer)
	{
		if (dataSetsIndexer == null) throw new IllegalArgumentException("DataSetsIndexer is null");
		this.dataSetsIndexer = dataSetsIndexer;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event)
	{
		try
		{
			if (event instanceof DataSetImportedEvent)
			{
				DataSetImportedEvent dataSetImportedEvent = (DataSetImportedEvent) event;
				dataSetsIndexer.index(Arrays.asList(dataSetImportedEvent.getDataSetId()));
			}
		}
		catch (TableException e)
		{
			throw new RuntimeException(e);
		}
	}
}
