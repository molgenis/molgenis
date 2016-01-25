package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;

@Component
public class MolgenisTokenMetaData extends DefaultEntityMetaData
{

	public static final String ENTITY_NAME = "MolgenisToken";

	public MolgenisTokenMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(MolgenisToken.ID).setAuto(true).setVisible(false).setDescription("").setIdAttribute(true)
				.setNillable(false);
		addAttribute(MolgenisToken.MOLGENIS_USER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData())
				.setAggregateable(true).setDescription("").setNillable(false);
		addAttribute(MolgenisToken.TOKEN).setLabel("Token").setUnique(true).setDescription("").setLabelAttribute(true)
				.setNillable(false);
		addAttribute(MolgenisToken.EXPIRATIONDATE).setDataType(DATETIME).setLabel("Expiration date").setNillable(true)
				.setDescription("When expiration date is null it will never expire");
		addAttribute(MolgenisToken.CREATIONDATE).setDataType(DATETIME).setLabel("Creation date").setAuto(true)
				.setReadOnly(true).setDescription("").setNillable(false);
		addAttribute(MolgenisToken.DESCRIPTION).setDataType(TEXT).setLabel("Description").setNillable(true)
				.setDescription("");

	}
}
