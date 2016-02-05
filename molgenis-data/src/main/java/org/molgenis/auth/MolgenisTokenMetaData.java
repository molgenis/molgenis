package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class MolgenisTokenMetaData extends DefaultEntityMetaData
{

	public static final String ENTITY_NAME = "MolgenisToken";

	public MolgenisTokenMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(MolgenisToken.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MolgenisToken.MOLGENIS_USER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData())
				.setAggregateable(true).setDescription("").setNillable(false);
		addAttribute(MolgenisToken.TOKEN, ROLE_LABEL).setLabel("Token").setUnique(true).setDescription("")
				.setNillable(false);
		addAttribute(MolgenisToken.EXPIRATIONDATE).setDataType(DATETIME).setLabel("Expiration date").setNillable(true)
				.setDescription("When expiration date is null it will never expire");
		addAttribute(MolgenisToken.CREATIONDATE).setDataType(DATETIME).setLabel("Creation date").setAuto(true)
				.setReadOnly(true).setDescription("").setNillable(false);
		addAttribute(MolgenisToken.DESCRIPTION).setDataType(TEXT).setLabel("Description").setNillable(true)
				.setDescription("");

	}
}
