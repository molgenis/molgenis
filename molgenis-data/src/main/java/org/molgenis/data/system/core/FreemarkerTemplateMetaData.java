package org.molgenis.data.system.core;

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
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false)
				.setDescription("automatically generated internal id, only for internal use.");
		addAttribute(NAME, ROLE_LABEL).setDescription("Name of the entity").setNillable(false).setUnique(true);
		addAttribute(VALUE).setDataType(SCRIPT).setNillable(false);
	}
}
