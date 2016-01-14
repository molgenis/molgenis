package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.BOOL;

@Component
public class MolgenisGroupMetaData extends DefaultEntityMetaData
{
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String ACTIVE = "active";

	public MolgenisGroupMetaData()
	{
		super("MolgenisGroup");

		setExtends(new AuthorityMetaData());
		addAttribute(ID).setAuto(true).setVisible(false).setDescription("").setIdAttribute(true).setNillable(false)
				.setLabelAttribute(true);
		addAttribute(NAME).setLabel("Name").setDescription("").setLookupAttribute(true).setNillable(false);
		addAttribute(ACTIVE).setLabel("Active").setDataType(BOOL).setDefaultValue("true")
				.setDescription("Boolean to indicate whether this group is in use.").setAggregateable(true)
				.setNillable(false);
	}
}
