package org.molgenis.dataexplorer.search;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.util.DataSetImportedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Indexes not yet indexed DataSets at application startup and when a dataset is imported
 * 
 * @author erwin
 * 
 */
public class StartUpIndexer implements ApplicationListener<ApplicationEvent>
{
	@Autowired
	private DataSetsIndexer dataSetsIndexer;

	// Index new datasets after the app config is loaded
	@Override
	public void onApplicationEvent(ApplicationEvent event)
	{

		try
		{
			if (event instanceof ContextRefreshedEvent)
			{
				dataSetsIndexer.indexNew();
			}
			else if (event instanceof DataSetImportedEvent)
			{
				DataSetImportedEvent dataSetImportedEvent = (DataSetImportedEvent) event;
				dataSetsIndexer.index(dataSetImportedEvent.getDataSetIdentifier());
			}
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		catch (TableException e)
		{
			throw new RuntimeException(e);
		}

	}

}
