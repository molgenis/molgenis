package org.molgenis.framework.tupletable;

import org.molgenis.framework.db.Database;

public interface DatabaseTupleTable
{
	public Database getDb();

	public void setDb(Database db);
}
