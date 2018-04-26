package org.molgenis.bootstrap.populate;

import org.molgenis.data.DataService;
import org.molgenis.data.i18n.LocalizationPopulator;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.i18n.LanguageService;
import org.molgenis.i18n.PropertiesMessageSource;
import org.molgenis.i18n.properties.AllPropertiesMessageSource;
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
	private final DataService dataService;
	private final LanguageFactory languageFactory;
	private final LocalizationPopulator localizationPopulator;
	private final List<PropertiesMessageSource> localizationMessageSources;

	public I18nPopulator(DataService dataService, LanguageFactory languageFactory,
			LocalizationPopulator localizationPopulator, List<PropertiesMessageSource> localizationMessageSources)
	{
		this.languageFactory = requireNonNull(languageFactory);
		this.dataService = requireNonNull(dataService);
		this.localizationPopulator = requireNonNull(localizationPopulator);
		this.localizationMessageSources = requireNonNull(localizationMessageSources);
	}

	/**
	 * Populates dataService with localization strings from property files on the classpath.
	 * <p>
	 * N.B. If you want to add a namespace with a localization resourcebundle, you need to
	 * add a PropertiesMessageSource bean to the spring context for that namespace.
	 */
	public void populateL10nStrings()
	{
		AllPropertiesMessageSource allPropertiesMessageSource = new AllPropertiesMessageSource();
		String[] namespaces = localizationMessageSources.stream()
														.map(PropertiesMessageSource::getNamespace)
														.toArray(String[]::new);
		allPropertiesMessageSource.addMolgenisNamespaces(namespaces);
		localizationPopulator.populateLocalizationStrings(allPropertiesMessageSource);
	}

	/**
	 * Populate data store with default languages
	 */
	public void populateLanguages()
	{
		dataService.add(LANGUAGE,
				languageFactory.create(LanguageService.DEFAULT_LANGUAGE_CODE, LanguageService.DEFAULT_LANGUAGE_NAME,
						true));
		dataService.add(LANGUAGE,
				languageFactory.create("nl", new Locale("nl").getDisplayName(new Locale("nl")), false));
		dataService.add(LANGUAGE,
				languageFactory.create("pt", new Locale("pt").getDisplayName(new Locale("pt")), false));
		dataService.add(LANGUAGE,
				languageFactory.create("es", new Locale("es").getDisplayName(new Locale("es")), false));
		dataService.add(LANGUAGE,
				languageFactory.create("de", new Locale("de").getDisplayName(new Locale("de")), false));
		dataService.add(LANGUAGE,
				languageFactory.create("it", new Locale("it").getDisplayName(new Locale("it")), false));
		dataService.add(LANGUAGE,
				languageFactory.create("fr", new Locale("fr").getDisplayName(new Locale("fr")), false));
		dataService.add(LANGUAGE, languageFactory.create("xx", "My language", false));
	}
}
