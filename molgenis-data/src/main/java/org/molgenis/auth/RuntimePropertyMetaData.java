package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.system.core.RuntimeProperty;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.TEXT;

@Component
public class RuntimePropertyMetaData extends DefaultEntityMetaData
{

	public static final String ENTITY_NAME = "RuntimeProperty";

	public RuntimePropertyMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(RuntimeProperty.ID).setAuto(true).setVisible(false)
				.setDescription("automatically generated internal id, only for internal use.").setIdAttribute(true)
				.setNillable(false);
		addAttribute(RuntimeProperty.NAME).setUnique(true).setDescription("").setLabelAttribute(true)
				.setNillable(false);
		addAttribute(RuntimeProperty.VALUE).setNillable(false).setDescription("").setDataType(TEXT);
	}
}
