package org.molgenis.app;

import org.molgenis.app.controller.HomeController;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.security.MolgenisSecurityWebAppDatabasePopulatorService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebAppDatabasePopulatorServiceImpl implements WebAppDatabasePopulatorService
{
	private final DataService dataService;
	private final MolgenisSecurityWebAppDatabasePopulatorService molgenisSecurityWebAppDatabasePopulatorService;

	@Autowired
	public WebAppDatabasePopulatorServiceImpl(DataService dataService,
			MolgenisSecurityWebAppDatabasePopulatorService molgenisSecurityWebAppDatabasePopulatorService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;

		if (molgenisSecurityWebAppDatabasePopulatorService == null) throw new IllegalArgumentException(
				"MolgenisSecurityWebAppDatabasePopulator is null");
		this.molgenisSecurityWebAppDatabasePopulatorService = molgenisSecurityWebAppDatabasePopulatorService;

	}

	@Override
	@Transactional
	@RunAsSystem
	public void populateDatabase()
	{
		molgenisSecurityWebAppDatabasePopulatorService.populateDatabase(this.dataService, HomeController.ID);

		MolgenisUser anonymousUser = molgenisSecurityWebAppDatabasePopulatorService.getAnonymousUser();
		UserAuthority anonymousHomeAuthority = new UserAuthority();
		anonymousHomeAuthority.setMolgenisUser(anonymousUser);
		anonymousHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + HomeController.ID.toUpperCase());
		dataService.add(UserAuthority.ENTITY_NAME, anonymousHomeAuthority);
	}

	@Override
	@Transactional
	@RunAsSystem
	public boolean isDatabasePopulated()
	{
		return dataService.count(MolgenisUser.ENTITY_NAME, new QueryImpl()) > 0;
	}
}