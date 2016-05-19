package org.molgenis.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LOOKUP;

import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisGroupMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "MolgenisGroup";

	private AuthorityMetaData authorityMetaData;

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		setExtends(authorityMetaData);
		addAttribute(MolgenisGroup.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MolgenisGroup.NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name").setDescription("")
				.setNillable(false);
		addAttribute(MolgenisGroup.ACTIVE).setLabel("Active").setDataType(BOOL).setDefaultValue("true")
				.setDescription("Boolean to indicate whether this group is in use.").setAggregatable(true)
				.setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setAuthorityMetaData(AuthorityMetaData authorityMetaData)
	{
		this.authorityMetaData = requireNonNull(authorityMetaData);
	}
}
