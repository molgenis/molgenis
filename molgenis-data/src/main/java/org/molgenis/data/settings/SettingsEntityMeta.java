package org.molgenis.data.settings;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.Package;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class SettingsEntityMeta extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "settings";
	public static final String PACKAGE_NAME = "settings";
	public static final Package PACKAGE_SETTINGS = new PackageImpl(PACKAGE_NAME, "Application and plugin settings");
	public static final String ID = "id";

	public SettingsEntityMeta()
	{
		super(ENTITY_NAME);
		setAbstract(true);
		setPackage(PACKAGE_SETTINGS);
		addAttribute(ID, ROLE_ID).setLabel("Id").setVisible(false);
	}
}
