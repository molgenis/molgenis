package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LOOKUP;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class MolgenisGroupMetaData extends DefaultEntityMetaData
{

	public static final String ENTITY_NAME = "MolgenisGroup";

	public MolgenisGroupMetaData()
	{
		super(ENTITY_NAME);

		setExtends(new AuthorityMetaData());
		addAttribute(MolgenisGroup.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MolgenisGroup.NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name").setDescription("")
				.setNillable(false);
		addAttribute(MolgenisGroup.ACTIVE).setLabel("Active").setDataType(BOOL).setDefaultValue("true")
				.setDescription("Boolean to indicate whether this group is in use.").setAggregateable(true)
				.setNillable(false);
	}
}
