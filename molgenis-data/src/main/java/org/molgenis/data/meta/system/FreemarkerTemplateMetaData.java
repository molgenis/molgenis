package org.molgenis.data.meta.system;

import static org.molgenis.MolgenisFieldTypes.SCRIPT;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class FreemarkerTemplateMetaData extends DefaultEntityMetaData
{
	public static final String ID = "id";
	public static final String NAME = "Name";
	public static final String VALUE = "Value";
	public static final String ENTITY_NAME = "FreemarkerTemplate";

	public FreemarkerTemplateMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false)
				.setDescription("automatically generated internal id, only for internal use.");
		addAttribute(NAME, ROLE_LABEL).setDescription("Name of the entity").setNillable(false).setUnique(true);
		addAttribute(VALUE).setDataType(SCRIPT).setNillable(false).setDescription("");
	}
}
