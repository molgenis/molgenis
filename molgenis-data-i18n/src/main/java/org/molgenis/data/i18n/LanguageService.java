package org.molgenis.data.i18n;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.i18n.LocalizationService.NAMESPACE_ALL;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

@Service
public class LanguageService
{
	public static final String LANGUAGE_CODE_EN = "en";
	public static final String LANGUAGE_CODE_NL = "nl";
	public static final String LANGUAGE_CODE_DE = "de";
	public static final String LANGUAGE_CODE_ES = "es";
	public static final String LANGUAGE_CODE_IT = "it";
	public static final String LANGUAGE_CODE_PT = "pt";
	public static final String LANGUAGE_CODE_FR = "fr";
	public static final String LANGUAGE_CODE_XX = "xx";

	public static final String DEFAULT_LANGUAGE_CODE = LANGUAGE_CODE_EN;
	public static final String DEFAULT_LANGUAGE_NAME = "English";

	private final DataService dataService;
	private final AppSettings appSettings;
	private final LocalizationService localizationService;

	@Autowired
	public LanguageService(DataService dataService, AppSettings appSettings, LocalizationService localizationService)
	{
		this.dataService = requireNonNull(dataService);
		this.appSettings = requireNonNull(appSettings);
		this.localizationService = requireNonNull(localizationService);
	}

	/**
	 * "en": is default
	 * "xx": is a placeholder for having your own language
	 * "nl", "de", "es", "it", "pt", "fr": are extra languages
	 *
	 * @return
	 */
	public static Stream<String> getLanguageCodes()
	{
		return Stream.of(LANGUAGE_CODE_EN, LANGUAGE_CODE_NL, LANGUAGE_CODE_DE, LANGUAGE_CODE_ES, LANGUAGE_CODE_IT,
				LANGUAGE_CODE_PT, LANGUAGE_CODE_FR, LANGUAGE_CODE_XX);
	}

	/**
	 * Creates a localization ResourceBundle for the current user's language.
	 * <p>
	 * See {@link LocalizationMessageSource} documentation for lookup implementation details.
	 * <p>
	 * The ResourceBundle is a Spring {@link MessageSourceResourceBundle} which means that you cannot query its keys.
	 * Ask the {@link LocalizationService} instead.
	 */
	public MessageSourceResourceBundle getBundle()
	{
		return getBundle(getCurrentUserLanguageCode());
	}

	/**
	 * Creates a localization ResourceBundle for a specific language.
	 *
	 * @return MessageSourceResourceBundle
	 */
	public MessageSourceResourceBundle getBundle(String languageCode)
	{
		return new MessageSourceResourceBundle(getMessageSource(NAMESPACE_ALL), new Locale(languageCode));
	}

	/**
	 * Gets MessageSource for a namespace.
	 *
	 * @param namespace the namespace of the bundle, or NAMESPACE_ALL for all namespaces
	 * @return MessageSource for the specified language and namespace
	 */
	private MessageSource getMessageSource(String namespace)
	{
		return new LocalizationMessageSource(localizationService, namespace, appSettings);
	}

	/**
	 * Get the language code of the current user, returns the app default if not set
	 */
	public String getCurrentUserLanguageCode()
	{
		String currentUserName = SecurityUtils.getCurrentUsername();

		return runAsSystem(() ->
		{
			String languageCode = null;

			if (currentUserName != null)
			{
				Entity user = dataService.query(USER).eq("username", currentUserName).findOne();
				if (user != null)
				{
					languageCode = user.getString("languageCode");
					if ((languageCode != null) && (dataService.findOneById(LANGUAGE, languageCode) == null))
					{
						languageCode = null;
					}
				}
			}

			if (languageCode == null)
			{
				// Use app default
				languageCode = appSettings.getLanguageCode();
				if ((languageCode == null) || (dataService.findOneById(LANGUAGE, languageCode) == null))
				{
					languageCode = DEFAULT_LANGUAGE_CODE;
				}
			}

			return languageCode;
		});
	}

	public static boolean hasLanguageCode(String code)
	{
		return getLanguageCodes().anyMatch(e -> e.equals(code));
	}
}
