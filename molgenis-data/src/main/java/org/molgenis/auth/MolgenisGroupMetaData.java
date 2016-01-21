package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.BOOL;

@Component
public class MolgenisGroupMetaData extends DefaultEntityMetaData
{

	public static final String ENTITY_NAME = "MolgenisGroup";

	public MolgenisGroupMetaData()
	{
		super(ENTITY_NAME);

		setExtends(new AuthorityMetaData());
		addAttribute(MolgenisGroup.ID).setAuto(true).setVisible(false).setDescription("").setIdAttribute(true)
				.setNillable(false).setLabelAttribute(true);
		addAttribute(MolgenisGroup.NAME).setLabel("Name").setDescription("").setLookupAttribute(true)
				.setNillable(false);
		addAttribute(MolgenisGroup.ACTIVE).setLabel("Active").setDataType(BOOL).setDefaultValue("true")
				.setDescription("Boolean to indicate whether this group is in use.").setAggregateable(true)
				.setNillable(false);
	}
}
