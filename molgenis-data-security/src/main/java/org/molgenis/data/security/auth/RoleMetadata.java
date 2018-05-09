package org.molgenis.data.security.auth;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

@Component
public class RoleMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Role";
	public static final String ROLE = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String ACTIVE = "active";

	private final SecurityPackage securityPackage;

	RoleMetadata(SecurityPackage securityPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
	}

	@Override
	public void init()
	{
		setLabel("Role");
		setPackage(securityPackage);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name").setNillable(false).setUnique(true);
		addAttribute(ACTIVE).setLabel("Active")
							.setDataType(BOOL)
							.setDefaultValue("true")
							.setDescription("Boolean to indicate whether this role is in use.")
							.setAggregatable(true)
							.setNillable(false);
	}
}
