package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LOOKUP;

import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.stereotype.Component;

@Component
public class MolgenisGroupMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "MolgenisGroup";

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		setExtends(new AuthorityMetaData());
		addAttribute(MolgenisGroup.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MolgenisGroup.NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name").setDescription("")
				.setNillable(false);
		addAttribute(MolgenisGroup.ACTIVE).setLabel("Active").setDataType(BOOL).setDefaultValue("true")
				.setDescription("Boolean to indicate whether this group is in use.").setAggregatable(true)
				.setNillable(false);
	}
}
