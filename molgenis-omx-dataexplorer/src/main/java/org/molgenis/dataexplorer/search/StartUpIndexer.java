package org.molgenis.dataexplorer.search;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Indexes not yet indexed DataSets at application startup
 * 
 * @author erwin
 * 
 */
public class StartUpIndexer implements ApplicationListener<ApplicationContextEvent>
{
	@Autowired
	private DataSetsIndexer dataSetsIndexer;

	// Index new datasets after the app config is loaded
	@Override
	public void onApplicationEvent(ApplicationContextEvent event)
	{
		if (event instanceof ContextRefreshedEvent)
		{
			try
			{
				System.out.println("XXXXXXXXX1");
				dataSetsIndexer.indexNew();
				System.out.println("XXXXXXXXX2");
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

}
