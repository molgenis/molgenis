package org.molgenis.data.i18n;

import static java.util.stream.Collectors.toList;
import static org.molgenis.util.EntityUtils.asStream;

import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LanguageService
{
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
	public List<String> getLanguageCodes()
	{
		return asStream(dataService.findAll(LanguageMetaData.ENTITY_NAME)).map(e -> e.getString(LanguageMetaData.CODE))
				.collect(toList());
	}

	/**
	 * Get the language code of the current user, returns the app default if not set
	 */
	public String getCurrentUserLanguageCode()
	{
		String languageCode = null;
		String currentUserName = SecurityUtils.getCurrentUsername();
		if (currentUserName != null)
		{
			Entity user = dataService.query("MolgenisUser").eq("username", currentUserName).findOne();
			if (user != null)
			{
				languageCode = user.getString("languageCode");
			}
		}

		if (languageCode == null)
		{
			// Use app default
			languageCode = appSettings.getLanguageCode();
		}

		return languageCode;
	}
}
