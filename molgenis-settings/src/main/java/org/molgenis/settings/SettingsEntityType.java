package org.molgenis.settings;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.settings.SettingsPackage.PACKAGE_SETTINGS;

@Component
public class SettingsEntityType extends SystemEntityType
{
	private static final String SIMPLE_NAME = "settings";
	public static final String SETTINGS = PACKAGE_SETTINGS + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";

	private SettingsPackage settingsPackage;

	public SettingsEntityType()
	{
		super(SIMPLE_NAME, PACKAGE_SETTINGS);
	}

	@Override
	public void init()
	{
		setLabel("Settings");
		setAbstract(true);
		setPackage(settingsPackage);
		addAttribute(ID, ROLE_ID).setLabel("Id").setVisible(true);
	}

	@Autowired
	public void setSettingsPackage(SettingsPackage settingsPackage)
	{
		this.settingsPackage = settingsPackage;
	}
}
