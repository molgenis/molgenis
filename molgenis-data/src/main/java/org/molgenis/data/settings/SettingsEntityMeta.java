package org.molgenis.data.settings;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.settings.SettingsPackage.PACKAGE_SETTINGS;

@Component
public class SettingsEntityMeta extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "settings";
	public static final String SETTINGS = PACKAGE_SETTINGS + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";

	private SettingsPackage settingsPackage;

	public SettingsEntityMeta()
	{
		super(SIMPLE_NAME, PACKAGE_SETTINGS);
	}

	@Override
	public void init()
	{
		setAbstract(true);
		setPackage(settingsPackage);
		addAttribute(ID, ROLE_ID).setLabel("Id").setVisible(false);
	}

	@Autowired
	public void setSettingsPackage(SettingsPackage settingsPackage)
	{
		this.settingsPackage = settingsPackage;
	}
}
