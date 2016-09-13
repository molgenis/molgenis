package org.molgenis.apps.model;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class AppMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "App";
	public static final String APP = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String APP_NAME = "appname";
	public static final String APP_URL = "appurl";
	public static final String SOURCES_DIRECTORY = "sourcesdirectory";
	public static final String ACTIVE = "active";

	public AppMetaData()
	{
		super(SIMPLE_NAME, APP);
	}

	@Override
	protected void init()
	{
		setLabel("App");
		addAttribute(APP_NAME, AttributeRole.ROLE_ID).setNillable(false).setLabel("App name")
				.setDescription("The name of the app");
		addAttribute(APP_URL).setDataType(HYPERLINK).setNillable(true).setLabel("App url")
				.setDescription("URL with which the app can be reached");
		addAttribute(SOURCES_DIRECTORY).setDataType(FILE).setNillable(true).setLabel("Source directory")
				.setDescription("The directory containing all the source files for this app");
		addAttribute(ACTIVE).setDataType(BOOL).setLabel("Active app?")
				.setDescription("Boolean determining whether an app is active and available for in the menu");

	}
}
