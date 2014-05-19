package org.molgenis.security;

import org.molgenis.data.DataService;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisUser;

public interface MolgenisSecurityWebAppDatabasePopulatorService
{
	void populateDatabase(DataService dataService, String homeControllerId);

	MolgenisUser getAnonymousUser();

	MolgenisUser getUserAdmin();

	MolgenisGroup getAllUsersGroup();
}