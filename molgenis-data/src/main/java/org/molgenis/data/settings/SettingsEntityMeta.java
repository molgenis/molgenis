package org.molgenis.data.settings;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.Package;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingsEntityMeta extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "settings";
	public static final String PACKAGE_NAME = "settings";
	public static final String ID = "id";

	private SettingsPackage settingsPackage;

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		setAbstract(true);
		setPackage(settingsPackage);
		addAttribute(ID, ROLE_ID).setLabel("Id").setVisible(false);
	}

	@Autowired
	public void setSettingsPackage(SettingsPackage settingsPackage) {
		this.settingsPackage = settingsPackage;
	}
}
