package org.molgenis.app;

import org.molgenis.app.controller.HomeController;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.i18n.LanguageFactory;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.security.MolgenisSecurityWebAppDatabasePopulatorService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.data.i18n.LanguageMetaData.*;

@Service
public class WebAppDatabasePopulatorServiceImpl implements WebAppDatabasePopulatorService
{
	private final DataService dataService;
	private final MolgenisSecurityWebAppDatabasePopulatorService molgenisSecurityWebAppDatabasePopulatorService;
	private final UserAuthorityFactory userAuthorityFactory;
	private final LanguageFactory languageFactory;

	@Autowired
	public WebAppDatabasePopulatorServiceImpl(DataService dataService,
			MolgenisSecurityWebAppDatabasePopulatorService molgenisSecurityWebAppDatabasePopulatorService,
			UserAuthorityFactory userAuthorityFactory, LanguageFactory languageFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.molgenisSecurityWebAppDatabasePopulatorService = requireNonNull(
				molgenisSecurityWebAppDatabasePopulatorService);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.languageFactory = requireNonNull(languageFactory);
	}

	@Override
	@Transactional
	@RunAsSystem
	public void populateDatabase()
	{
		molgenisSecurityWebAppDatabasePopulatorService.populateDatabase(this.dataService, HomeController.ID);

		MolgenisUser anonymousUser = molgenisSecurityWebAppDatabasePopulatorService.getAnonymousUser();
		UserAuthority anonymousHomeAuthority = userAuthorityFactory.create();
		anonymousHomeAuthority.setMolgenisUser(anonymousUser);
		anonymousHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + HomeController.ID.toUpperCase());
		dataService.add(USER_AUTHORITY, anonymousHomeAuthority);

		// add default language
		dataService.add(LANGUAGE, languageFactory.create(DEFAULT_LANGUAGE_CODE, DEFAULT_LANGUAGE_NAME));
	}

	@Override
	@Transactional
	@RunAsSystem
	public boolean isDatabasePopulated()
	{
		return dataService.count(MOLGENIS_USER) > 0;
	}
}