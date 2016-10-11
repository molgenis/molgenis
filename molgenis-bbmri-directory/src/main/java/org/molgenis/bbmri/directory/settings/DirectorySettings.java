package org.molgenis.bbmri.directory.settings;

import org.molgenis.bbmri.directory.controller.DirectoryController;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

/**
 * Directory Settings
 */
@Component
public class DirectorySettings extends DefaultSettingsEntity
{

	private static final long serialVersionUID = 1L;
	public static final String NEGOTIATOR_URL = "negotiator-url";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	private static final String NEGOTIATOR_URL_DEFALUT = "https://bbmri-demo.mitro.dkfz.de/hackathon/api/directory/create_query";
	private static final String ID = DirectoryController.ID;

	public DirectorySettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Directory settings");
			setDescription("Settings for the Directory HACK POC");
			addAttribute(NEGOTIATOR_URL).setLabel("Negotiator endpoint url").setDefaultValue(NEGOTIATOR_URL_DEFALUT);
			addAttribute(USERNAME).setLabel("Username");
			addAttribute(PASSWORD).setLabel("Password");
		}
	}
}
