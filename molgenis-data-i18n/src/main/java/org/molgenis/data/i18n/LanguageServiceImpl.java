package org.molgenis.data.i18n;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.stereotype.Service;

import java.util.Locale;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.i18n.LocalizationService.NAMESPACE_ALL;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

@Service
public class LanguageServiceImpl implements LanguageService
{
	public static final String DEFAULT_LANGUAGE_CODE = LANGUAGE_CODE_EN;
	public static final String DEFAULT_LANGUAGE_NAME = "English";

	private final DataService dataService;
	private final AppSettings appSettings;
	private final LocalizationService localizationService;

	public LanguageServiceImpl(DataService dataService, AppSettings appSettings,
			LocalizationService localizationService)
	{
		this.dataService = requireNonNull(dataService);
		this.appSettings = requireNonNull(appSettings);
		this.localizationService = requireNonNull(localizationService);
	}

	@Override
	public String getString(String key)
	{
		return getBundle().getString(key);
	}

	/**
	 * Creates a localization ResourceBundle for the current user's language.
	 * <p>
	 * See {@link LocalizationMessageSource} documentation for lookup implementation details.
	 * <p>
	 * The ResourceBundle is a Spring {@link MessageSourceResourceBundle} which means that you cannot query its keys.
	 * Ask the {@link LocalizationService} instead.
	 */
	@Override
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
	@Override
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
		return LanguageService.getLanguageCodes().anyMatch(e -> e.equals(code));
	}
}
