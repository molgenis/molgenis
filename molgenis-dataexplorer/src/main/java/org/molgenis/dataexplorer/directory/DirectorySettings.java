package org.molgenis.dataexplorer.directory;

import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class DirectorySettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	public static final String NEGOTIATOR_URL = "negotiator-url";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	private static final String NEGOTIATOR_URL_DEFAULT = "https://bbmri-demo.mitro.dkfz.de/demo/api/directory/create_query";
	private static final String ID = DirectoryController.ID;

	public DirectorySettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
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
			addAttribute(NEGOTIATOR_URL).setLabel("Negotiator endpoint url").setDefaultValue(NEGOTIATOR_URL_DEFAULT);
			addAttribute(USERNAME).setLabel("Username");
			addAttribute(PASSWORD).setLabel("Password");
		}
	}
}