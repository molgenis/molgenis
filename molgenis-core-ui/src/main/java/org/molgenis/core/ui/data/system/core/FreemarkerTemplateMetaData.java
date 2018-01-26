package org.molgenis.core.ui.data.system.core;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static org.molgenis.data.meta.AttributeType.SCRIPT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class FreemarkerTemplateMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "FreemarkerTemplate";
	public static final String FREEMARKER_TEMPLATE = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private static final String REGEX_NAME = "/^view-.*\\.ftl$/";

	public static final String ID = "id";
	public static final String NAME = "Name";
	public static final String VALUE = "Value";

	FreemarkerTemplateMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		setLabel("Freemarker template");

		addAttribute(ID, ROLE_ID).setLabel("Id").setAuto(true).setVisible(false);
		addAttribute(NAME, ROLE_LABEL).setLabel("Name")
									  .setDescription("Template name (must start with 'view-' and end with '.ftl')")
									  .setNillable(false)
									  .setUnique(true)
									  .setValidationExpression("$('" + NAME + "').matches(" + REGEX_NAME + ").value()");
		addAttribute(VALUE).setLabel("Value").setDataType(SCRIPT).setNillable(false);
	}
}
