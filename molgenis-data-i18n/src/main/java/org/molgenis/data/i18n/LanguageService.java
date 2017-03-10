package org.molgenis.data.i18n;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
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

	private final DataService dataService;
	private final AppSettings appSettings;
	public static final String DEFAULT_LANGUAGE_CODE = LANGUAGE_CODE_EN;
	public static final String DEFAULT_LANGUAGE_NAME = "English";

	@Autowired
	public LanguageService(DataService dataService, AppSettings appSettings)
	{
		this.dataService = dataService;
		this.appSettings = appSettings;
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
	 * Get ResourceBundle for a language
	 */
	public ResourceBundle getBundle(String languageCode)
	{
		return ResourceBundle.getBundle(I18N_STRING, new Locale(languageCode),
				new MolgenisResourceBundleControl(dataService, appSettings));
	}

	/**
	 * Get ResourceBundle for the current user language
	 */
	public ResourceBundle getBundle()
	{
		return getBundle(getCurrentUserLanguageCode());
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
