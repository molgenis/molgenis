package org.molgenis.app.manager.meta;

import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class AppMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "App";
	public static final String APP = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String DESCRIPTION = "description";
	public static final String IS_ACTIVE = "isActive";

	public static final String APP_VERSION = "appVersion";
	public static final String API_DEPENDENCY = "apiDependency";
	public static final String TEMPLATE_CONTENT = "templateContent";

	public static final String URI = "uri";
	public static final String APP_CONFIG = "appConfig";
	public static final String INCLUDE_MENU_AND_FOOTER = "includeMenuAndFooter";

	private final FileMetaMetaData fileMetaMetaData;

	public AppMetadata(FileMetaMetaData fileMetaMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.fileMetaMetaData = requireNonNull(fileMetaMetaData);
	}

	@Override
	protected void init()
	{
		setLabel("App");

		// App info
		addAttribute(ID, ROLE_ID).setAuto(true).setLabel("Identifier");
		addAttribute(LABEL, ROLE_LABEL, ROLE_LOOKUP).setLabel("Label").setNillable(false);
		addAttribute(DESCRIPTION, ROLE_LOOKUP).setDataType(TEXT).setNillable(true).setLabel("Description");
		addAttribute(IS_ACTIVE).setDataType(BOOL)
							   .setLabel("Active")
							   .setNillable(false)
							   .setDefaultValue(Boolean.FALSE.toString());

		// App resources
		addAttribute(APP_VERSION).setNillable(true).setLabel("App version");
		addAttribute(API_DEPENDENCY).setNillable(true).setLabel("API dependency version");
		addAttribute(TEMPLATE_CONTENT).setDataType(HTML).setNillable(false).setLabel("Template content");

		// App configuration
		addAttribute(URI, ROLE_LOOKUP).setLabel("URI")
									  .setDescription("The URI used to point at this app. I.e. /app/{uri}")
									  .setNillable(false)
									  .setUnique(true);
		addAttribute(APP_CONFIG).setDataType(TEXT).setNillable(true).setLabel("Runtime Application configuration");
		addAttribute(INCLUDE_MENU_AND_FOOTER).setDataType(BOOL)
											 .setLabel("Include a menu above your app and a footer below")
											 .setNillable(false)
											 .setDefaultValue(Boolean.FALSE.toString());
	}
}
