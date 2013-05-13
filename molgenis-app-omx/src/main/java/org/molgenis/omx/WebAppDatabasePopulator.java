package org.molgenis.omx;

import org.molgenis.framework.db.Database;

import app.MolgenisDatabasePopulator;

public class WebAppDatabasePopulator extends MolgenisDatabasePopulator
{
	@Override
	protected void initializeApplicationDatabase(Database database) throws Exception
	{
		// noop
	}
}