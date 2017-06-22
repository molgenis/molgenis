package org.molgenis.data.security;

import org.molgenis.auth.SecurityPackage;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.auth.TokenMetaData.DESCRIPTION;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class AclTestMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "AclTest2";
	public static final String ACL_TEST = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String LABEL = "label";

	private final SecurityPackage securityPackage;

	@Autowired
	AclTestMetadata(SecurityPackage securityPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
	}

	@Override
	public void init()
	{
		setLabel("ACL test");
		setPackage(securityPackage);
		setEntityLevelSecurity(true);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
		addAttribute(LABEL, ROLE_LABEL).setNillable(false).setAggregatable(true);
		addAttribute(DESCRIPTION).setDataType(TEXT).setNillable(true);
	}
}