package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.data.system.core.RuntimeProperty;
import org.springframework.stereotype.Component;

@Component
public class RuntimePropertyMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "RuntimeProperty";

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		addAttribute(RuntimeProperty.ID, ROLE_ID).setAuto(true).setVisible(false)
				.setDescription("automatically generated internal id, only for internal use.");
		addAttribute(RuntimeProperty.NAME, ROLE_LABEL).setUnique(true).setDescription("").setNillable(false);
		addAttribute(RuntimeProperty.VALUE).setNillable(false).setDescription("").setDataType(TEXT);
	}
}
