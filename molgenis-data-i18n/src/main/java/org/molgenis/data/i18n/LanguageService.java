package org.molgenis.data.i18n;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.i18n.model.LanguageMetaData;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.stream.Collectors.toList;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetaData.DEFAULT_LANGUAGE_CODE;
import static org.molgenis.data.i18n.model.LanguageMetaData.LANGUAGE;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

@Service
public class LanguageService
{
	public static final String FALLBACK_LANGUAGE = "en";
	private final DataService dataService;
	private final AppSettings appSettings;

	@Autowired
	public LanguageService(DataService dataService, AppSettings appSettings)
	{
		this.dataService = dataService;
		this.appSettings = appSettings;
	}

	/**
	 * Get all registered language codes
	 */
	@RunAsSystem
	public List<String> getLanguageCodes()
	{
		return dataService.findAll(LANGUAGE).map(e -> e.getString(LanguageMetaData.CODE))
				.collect(toList());
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

		return runAsSystem(() -> {
			String languageCode = null;

			if (currentUserName != null)
			{
				Entity user = dataService.query(MOLGENIS_USER).eq("username", currentUserName).findOne();
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
}
