package org.molgenis.search;

import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.util.Entity;

/**
 * Base class for SearchService implementations
 * 
 * @author erwin
 * 
 */
public abstract class AbstractSearchService implements SearchService
{
	protected final Logger log = Logger.getLogger(getClass());

	@Override
	public void indexDatabase(Database db) throws DatabaseException
	{
		log.info("Start indexing database");

		for (Class<? extends Entity> clazz : db.getEntityClasses())
		{
			String simpleName = clazz.getSimpleName();
			List<? extends Entity> entities = db.find(clazz);
			if ((entities != null) && !entities.isEmpty())
			{
				log.info("Indexing [" + simpleName + "]. Count=" + entities.size());

				updateIndex(simpleName, db.find(clazz));
			}
		}

		log.info("Indexing done");
	}

}
