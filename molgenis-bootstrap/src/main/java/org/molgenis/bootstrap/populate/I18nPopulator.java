package org.molgenis.bootstrap.populate;

import org.molgenis.data.DataService;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.i18n.LocalizationService;
import org.molgenis.data.i18n.PropertiesMessageSource;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;

/**
 * Imports l10n strings from registered {@link PropertiesMessageSource} beans at startup.
 * <p>
 * Only adds new strings, does not update existing ones, because otherwise the ones you have changed using the
 * dataexplorer will be overwritten again on the next startup.
 */
@Component
public class I18nPopulator
{
	private static final Logger LOG = LoggerFactory.getLogger(I18nPopulator.class);

	private final DataService dataService;
	private final LanguageFactory languageFactory;
	private final List<PropertiesMessageSource> localizationMessageSources;
	private final LocalizationService localizationService;

	@Autowired
	public I18nPopulator(DataService dataService, LanguageFactory languageFactory,
			List<PropertiesMessageSource> localizationMessageSources, LocalizationService localizationService)
	{
		this.languageFactory = requireNonNull(languageFactory);
		this.dataService = requireNonNull(dataService);
		this.localizationMessageSources = requireNonNull(localizationMessageSources);
		this.localizationService = requireNonNull(localizationService);
	}

	/**
	 * Populates dataService with localization strings from property files on the classpath.
	 * <p>
	 * N.B. If you want to add a namespace with a localization resourcebundle, you need to
	 * add a PropertiesMessageSource bean to the spring context for that namespace.
	 */
	public void populateL10nStrings()
	{
		localizationMessageSources.forEach(localizationService::populateLocalizationStrings);
	}

	/**
	 * Populate data store with default languages
	 */
	public void populateLanguages()
	{
		dataService.add(LANGUAGE, languageFactory
				.create(LanguageService.DEFAULT_LANGUAGE_CODE, LanguageService.DEFAULT_LANGUAGE_NAME, true));
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
		dataService.add(LANGUAGE, languageFactory.create("xx", "My language", false));
	}
}
