package org.molgenis.data.meta.system;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.SCRIPT;

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
		addAttribute(ID).setAuto(true).setVisible(false)
				.setDescription("automatically generated internal id, only for internal use.").setIdAttribute(true).setNillable(false);
		addAttribute(NAME).setDescription("Name of the entity").setNillable(false).setLabelAttribute(true).setUnique(true);
		addAttribute(VALUE).setDataType(SCRIPT).setNillable(false).setDescription("");
	}
}
