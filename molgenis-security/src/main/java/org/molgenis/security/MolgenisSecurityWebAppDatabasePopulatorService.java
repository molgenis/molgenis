package org.molgenis.security;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;

public interface MolgenisSecurityWebAppDatabasePopulatorService
{
	void populateDatabase(DataService dataService, String homeControllerId);

	MolgenisUser getAnonymousUser();
}