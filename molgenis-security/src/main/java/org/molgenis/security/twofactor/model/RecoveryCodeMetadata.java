package org.molgenis.security.twofactor.model;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.security.auth.SecurityPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

@Component
public class RecoveryCodeMetadata extends SystemEntityType
{
	public static final String ID = "id";
	public static final String USER_ID = "userId";
	public static final String CODE = "code";
	private static final String SIMPLE_NAME = "RecoveryCode";
	public static final String RECOVERY_CODE = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;
	private final SecurityPackage securityPackage;

	public RecoveryCodeMetadata(SecurityPackage securityPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
	}

	@Override
	public void init()
	{
		setLabel("Recovery Code");
		setPackage(securityPackage);

		setDescription("Codes for recovering an account when using two factor authorisation");

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setLabel("Identifier");
		addAttribute(USER_ID).setNillable(false).setLabel("User identifier");
		addAttribute(CODE).setNillable(false).setLabel("Recovery code");
	}
}
