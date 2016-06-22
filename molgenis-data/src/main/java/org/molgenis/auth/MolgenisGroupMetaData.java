package org.molgenis.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisGroupMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "MolgenisGroup";
	public static final String MOLGENIS_GROUP = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String ACTIVE = "active";

	private final SecurityPackage securityPackage;
	private final AuthorityMetaData authorityMetaData;

	@Autowired
	MolgenisGroupMetaData(SecurityPackage securityPackage, AuthorityMetaData authorityMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
		this.authorityMetaData = requireNonNull(authorityMetaData);
	}

	@Override
	public void init()
	{
		setPackage(securityPackage);

		setExtends(authorityMetaData);
		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name").setDescription("").setNillable(false);
		addAttribute(ACTIVE).setLabel("Active").setDataType(BOOL).setDefaultValue("true")
				.setDescription("Boolean to indicate whether this group is in use.").setAggregatable(true)
				.setNillable(false);
	}
}
