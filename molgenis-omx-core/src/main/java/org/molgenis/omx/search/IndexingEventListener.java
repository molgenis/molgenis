package org.molgenis.omx.search;

import java.util.Arrays;

import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.util.EntityImportedEvent;
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
		if (event instanceof EntityImportedEvent)
		{
			EntityImportedEvent entityImportedEvent = (EntityImportedEvent) event;
			String entityName = entityImportedEvent.getEntityName();
			if (entityName.equals(DataSet.ENTITY_NAME))
			{
				dataSetsIndexer.indexDataSets(Arrays.asList(entityImportedEvent.getEntityId()));
			}
			else if (entityName.equals(Protocol.ENTITY_NAME))
			{
				dataSetsIndexer.indexProtocols(Arrays.asList(entityImportedEvent.getEntityId()));
			}
		}
	}
}
