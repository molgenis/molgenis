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
	public static final String RESOURCE_ZIP = "resourceZip";
	public static final String TEMPLATE_CONTENT = "templateContent";
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
		addAttribute(ID, ROLE_ID).setLabel("Identifier")
								 .setDescription(
										 "Identifier used to create the URL of your app. URL will be /app/{identifier}");
		addAttribute(LABEL, ROLE_LABEL, ROLE_LOOKUP).setLabel("Label").setNillable(false);
		addAttribute(DESCRIPTION, ROLE_LOOKUP).setDataType(TEXT).setNillable(true).setLabel("Description");
		addAttribute(IS_ACTIVE).setDataType(BOOL)
							   .setLabel("Active")
							   .setNillable(false)
							   .setVisible(false)
							   .setDefaultValue(Boolean.FALSE.toString());

		// App resources
		addAttribute(RESOURCE_ZIP).setDataType(FILE)
								  .setRefEntity(fileMetaMetaData)
								  .setNillable(false)
								  .setLabel("Resource ZIP file")
								  .setDescription("ZIP file with JavaScript, CSS and image files required by the app");
		addAttribute(TEMPLATE_CONTENT).setDataType(HTML).setNillable(false).setLabel("Template content");

		// App configuration
		addAttribute(APP_CONFIG).setDataType(TEXT).setNillable(true).setLabel("Application config");
		addAttribute(INCLUDE_MENU_AND_FOOTER).setDataType(BOOL)
											 .setLabel("Include a menu above your app and a footer below");
	}
}
