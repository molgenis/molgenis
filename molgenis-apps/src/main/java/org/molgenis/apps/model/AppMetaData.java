package org.molgenis.apps.model;

import org.molgenis.core.ui.data.system.core.FreemarkerTemplateMetaData;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class AppMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "App";
	public static final String APP = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ICON_HREF = "iconHref";
	public static final String IS_ACTIVE = "isActive";
	public static final String LANDING_PAGE_HTML_TEMPLATE = "landingPageHtmlTemplate";
	public static final String RESOURCE_ZIP = "resourceZip";
	public static final String USE_FREEMARKER_TEMPLATE = "useFreemarkerTemplate";

	private final FileMetaMetaData fileMetaMetaData;
	private final FreemarkerTemplateMetaData freemarkerTemplateMetaData;

	public AppMetaData(FileMetaMetaData fileMetaMetaData, FreemarkerTemplateMetaData freemarkerTemplateMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
		this.fileMetaMetaData = requireNonNull(fileMetaMetaData);
		this.freemarkerTemplateMetaData = requireNonNull(freemarkerTemplateMetaData);
	}

	@Override
	protected void init()
	{
		setLabel("App");
		addAttribute(ID, ROLE_ID).setAuto(true).setLabel("Id");
		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name").setNillable(false).setUnique(true);
		addAttribute(DESCRIPTION, ROLE_LOOKUP).setDataType(TEXT).setNillable(true).setLabel("Description");
		addAttribute(ICON_HREF).setDataType(HYPERLINK)
							   .setNillable(true)
							   .setLabel("Icon URL")
							   .setDescription("Absolute or relative URL of the app icon");
		addAttribute(RESOURCE_ZIP).setDataType(FILE)
								  .setRefEntity(fileMetaMetaData)
								  .setNillable(true)
								  .setLabel("Resource ZIP file")
								  .setDescription("ZIP file with JavaScript, CSS and image files required by the app");
		addAttribute(IS_ACTIVE).setDataType(BOOL)
							   .setLabel("Active")
							   .setNillable(false)
							   .setVisible(false)
							   .setDefaultValue(Boolean.FALSE.toString());
		addAttribute(USE_FREEMARKER_TEMPLATE).setDataType(BOOL)
											 .setLabel("Use freemarker template as index.html")
											 .setNillable(false)
											 .setDefaultValue(Boolean.FALSE.toString())
											 .setDescription(
													 "When using a freemarker template, the index page of your app should be placed in "
															 + "the FreemarkerTemplates table with a name like: view-apps-yourname.ftl. "
															 + "When selecting no, please include an index.html in the root of your app.zip");
		addAttribute(LANDING_PAGE_HTML_TEMPLATE).setDataType(XREF)
												.setRefEntity(freemarkerTemplateMetaData)
												.setNillable(true)
												.setLabel("Landing page HTML template")
												.setVisibleExpression(
														"$('" + USE_FREEMARKER_TEMPLATE + "').eq(true).value()")
												.setValidationExpression(
														"$('" + LANDING_PAGE_HTML_TEMPLATE + "').value() != null || $('"
																+ USE_FREEMARKER_TEMPLATE + "').eq(false).value()")
												.setDescription("Landing page HTML FreeMarker template");
	}
}
