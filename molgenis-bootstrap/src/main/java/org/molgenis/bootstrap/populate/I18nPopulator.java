package org.molgenis.bootstrap.populate;

import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.i18n.messages.PropertiesMessageSource;
import org.molgenis.data.i18n.model.I18nString;
import org.molgenis.data.i18n.model.I18nStringFactory;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.model.I18nStringMetaData.MSGID;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;

/**
 * Imports l10n strings from registered {@link PropertiesMessageSource}s at startup.
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
	private final List<PropertiesMessageSource> molgenisMessageSources;
	private final I18nStringFactory i18nStringFactory;

	@Autowired
	public I18nPopulator(DataService dataService, LanguageFactory languageFactory,
			List<PropertiesMessageSource> molgenisMessageSources, I18nStringFactory i18nStringFactory)
	{
		this.languageFactory = requireNonNull(languageFactory);
		this.dataService = requireNonNull(dataService);
		this.molgenisMessageSources = requireNonNull(molgenisMessageSources);
		this.i18nStringFactory = requireNonNull(i18nStringFactory);
	}

	/**
	 * Populates dataService with internationalization strings from property files on the classpath.
	 */
	public void populateI18nStrings()
	{
		for (PropertiesMessageSource messageSource : molgenisMessageSources)
		{
			String namespace = messageSource.getNamespace();
			Set<String> messageIds = messageSource.getCodes();

			Set<String> existing = dataService.findAll(I18N_STRING,
					QueryImpl.IN(MSGID, messageIds).and().eq(I18nStringMetaData.NAMESPACE, namespace))
					.map(e -> e.getString(MSGID)).collect(toSet());

			Stream<I18nString> missing = Sets.difference(messageIds, existing).stream().map(msgId ->
			{
				I18nString result = i18nStringFactory.create();
				result.setMessageId(msgId);
				result.setNamespace(namespace);
				LanguageService.getLanguageCodes().forEach((language) ->
				{
					String message = messageSource.getMessage(language, msgId);
					if (message != null)
					{
						result.set(language, message);
					}
				});
				return result;
			});

			dataService.add(I18N_STRING, missing);
		}
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
