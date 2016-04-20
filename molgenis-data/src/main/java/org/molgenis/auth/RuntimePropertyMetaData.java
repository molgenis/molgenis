package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.system.core.RuntimeProperty;
import org.springframework.stereotype.Component;

@Component
public class RuntimePropertyMetaData extends EntityMetaData
{

	public static final String ENTITY_NAME = "RuntimeProperty";

	public RuntimePropertyMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(RuntimeProperty.ID, ROLE_ID).setAuto(true).setVisible(false)
				.setDescription("automatically generated internal id, only for internal use.");
		addAttribute(RuntimeProperty.NAME, ROLE_LABEL).setUnique(true).setDescription("").setNillable(false);
		addAttribute(RuntimeProperty.VALUE).setNillable(false).setDescription("").setDataType(TEXT);
	}
}
