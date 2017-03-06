package org.molgenis.apps.model;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.core.FreemarkerTemplateMetaData.FREEMARKER_TEMPLATE;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.molgenis.file.model.FileMetaMetaData.FILE_META;

@Component
public class AppMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "App";
	public static final String APP = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String APP_NAME = "name";
	public static final String APP_DESCRIPTION = "description";
	public static final String RESOURCE_FILES = "resourcefiles";
	public static final String ACTIVE = "active";
	public static final String APP_ICON_URL = "appiconurl";
	public static final String APP_INDEX_HTML = "appindexhtml";

	private final DataService dataService;

	@Autowired
	public AppMetaData(DataService dataService)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	protected void init()
	{
		setLabel("App");
		addAttribute(APP_NAME, AttributeRole.ROLE_ID).setNillable(false).setLabel("App name")
				.setDescription("The name of the app");
		addAttribute(APP_DESCRIPTION).setDataType(TEXT).setNillable(true).setLabel("App description");
		addAttribute(APP_ICON_URL).setDataType(HYPERLINK).setNillable(true).setLabel("URL for your App icon");
		addAttribute(RESOURCE_FILES).setDataType(FILE).setRefEntity(dataService.getEntityType(FILE_META))
				.setNillable(true).setLabel("Resource files in ZIP format")
				.setDescription("A zip containing all the javascript and css for this app");
		addAttribute(ACTIVE).setDataType(BOOL).setLabel("Active app?").setVisible(false)
				.setDescription("Boolean determining whether an app is active and available for in the menu")
				.setDefaultValue("FALSE");
		addAttribute(APP_INDEX_HTML).setDataType(XREF).setRefEntity(dataService.getEntityType(FREEMARKER_TEMPLATE))
				.setNillable(false).setLabel("HTML for your page")
				.setDescription("For any app, this is your main html page.");
	}
}
