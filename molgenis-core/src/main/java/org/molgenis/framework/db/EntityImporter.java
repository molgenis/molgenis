package org.molgenis.framework.db;

import java.io.IOException;

import org.molgenis.framework.db.Database.DatabaseAction;
import org.molgenis.io.TupleReader;

public interface EntityImporter
{
	public int importEntity(TupleReader tupleReader, Database db, DatabaseAction dbAction) throws IOException,
			DatabaseException;
}
