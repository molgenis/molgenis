package org.molgenis.framework.db;

import java.io.IOException;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.framework.db.Database.DatabaseAction;

public interface EntityImporter
{
	public int importEntity(Repository<? extends Entity> repository, Database db, DatabaseAction dbAction)
			throws IOException, DatabaseException;
}
