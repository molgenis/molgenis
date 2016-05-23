package org.molgenis.data.i18n;

import static java.util.stream.Collectors.toList;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
		return dataService.findAll(LanguageMetaData.ENTITY_NAME).map(e -> e.getString(LanguageMetaData.CODE))
				.collect(toList());
	}

	/**
	 * Get ResourceBundle for a language
	 */
	public ResourceBundle getBundle(String languageCode)
	{
		return ResourceBundle.getBundle(I18nStringMetaData.ENTITY_NAME, new Locale(languageCode),
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
				Entity user = dataService.query("MolgenisUser").eq("username", currentUserName).findOne();
				if (user != null)
				{
					languageCode = user.getString("languageCode");
					if ((languageCode != null)
							&& (dataService.findOne(LanguageMetaData.ENTITY_NAME, languageCode) == null))
					{
						languageCode = null;
					}
				}
			}

			if (languageCode == null)
			{
				// Use app default
				languageCode = appSettings.getLanguageCode();
				if ((languageCode == null) || (dataService.findOne(LanguageMetaData.ENTITY_NAME, languageCode) == null))
				{
					languageCode = FALLBACK_LANGUAGE;
				}
			}

			return languageCode;
		});
	}
}
