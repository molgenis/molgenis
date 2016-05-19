package org.molgenis.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisTokenMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "MolgenisToken";

	private MolgenisUserMetaData molgenisUserMetaData;

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		addAttribute(MolgenisToken.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MolgenisToken.MOLGENIS_USER).setDataType(XREF).setRefEntity(molgenisUserMetaData)
				.setAggregatable(true).setDescription("").setNillable(false);
		addAttribute(MolgenisToken.TOKEN, ROLE_LABEL).setLabel("Token").setUnique(true).setDescription("")
				.setNillable(false);
		addAttribute(MolgenisToken.EXPIRATIONDATE).setDataType(DATETIME).setLabel("Expiration date").setNillable(true)
				.setDescription("When expiration date is null it will never expire");
		addAttribute(MolgenisToken.CREATIONDATE).setDataType(DATETIME).setLabel("Creation date").setAuto(true)
				.setReadOnly(true).setDescription("").setNillable(false);
		addAttribute(MolgenisToken.DESCRIPTION).setDataType(TEXT).setLabel("Description").setNillable(true)
				.setDescription("");
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setMolgenisUserMetaData(MolgenisUserMetaData molgenisUserMetaData)
	{
		this.molgenisUserMetaData = requireNonNull(molgenisUserMetaData);
	}
}
