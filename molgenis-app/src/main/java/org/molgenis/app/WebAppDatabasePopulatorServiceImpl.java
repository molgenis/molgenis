package org.molgenis.app;

import org.molgenis.app.controller.HomeController;
import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.security.MolgenisSecurityWebAppDatabasePopulatorService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.ui.admin.user.UserAccountController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.i18n.model.LanguageMetaData.*;

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
		molgenisSecurityWebAppDatabasePopulatorService
				.populateDatabase(this.dataService, HomeController.ID, UserAccountController.ID);

		dataService.add(LANGUAGE, languageFactory.create(LanguageService.DEFAULT_LANGUAGE_CODE, LanguageService.DEFAULT_LANGUAGE_NAME, true));
		dataService
				.add(LANGUAGE, languageFactory.create("nl", new Locale("nl").getDisplayName(new Locale("nl")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("pt", new Locale("pt").getDisplayName(new Locale("pt")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("es", new Locale("es").getDisplayName(new Locale("es")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("de", new Locale("de").getDisplayName(new Locale("de")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("it", new Locale("it").getDisplayName(new Locale("it")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("fr", new Locale("fr").getDisplayName(new Locale("fr")), false));
		dataService
				.add(LANGUAGE, languageFactory.create("xx", "My language", false));
	}

	@Override
	@Transactional
	@RunAsSystem
	public boolean isDatabasePopulated()
	{
		return dataService.count(USER) > 0;
	}
}