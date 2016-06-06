package org.molgenis.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisTokenMetaData extends SystemEntityMetaDataImpl
{
	private static final String SIMPLE_NAME = "MolgenisToken";
	public static final String MOLGENIS_TOKEN = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String TOKEN = "token";
	public static final String ID = "id";
	public static final String MOLGENIS_USER = "molgenisUser";
	public static final String EXPIRATIONDATE = "expirationDate";
	public static final String CREATIONDATE = "creationDate";
	public static final String DESCRIPTION = "description";

	private final SecurityPackage securityPackage;

	private MolgenisUserMetaData molgenisUserMetaData;

	@Autowired
	MolgenisTokenMetaData(SecurityPackage securityPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
	}

	@Override
	public void init()
	{
		setPackage(securityPackage);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MOLGENIS_USER).setDataType(XREF).setRefEntity(molgenisUserMetaData)
				.setAggregatable(true).setDescription("").setNillable(false);
		addAttribute(TOKEN, ROLE_LABEL).setLabel("Token").setUnique(true).setDescription("")
				.setNillable(false);
		addAttribute(EXPIRATIONDATE).setDataType(DATETIME).setLabel("Expiration date").setNillable(true)
				.setDescription("When expiration date is null it will never expire");
		addAttribute(CREATIONDATE).setDataType(DATETIME).setLabel("Creation date").setAuto(true)
				.setReadOnly(true).setDescription("").setNillable(false);
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description").setNillable(true)
				.setDescription("");
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setMolgenisUserMetaData(MolgenisUserMetaData molgenisUserMetaData)
	{
		this.molgenisUserMetaData = requireNonNull(molgenisUserMetaData);
	}
}
