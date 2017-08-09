package org.molgenis.security.twofactor.meta;

import org.molgenis.auth.SecurityPackage;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class RecoveryCodeMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "RecoveryCode";
	public static final String RECOVERY_CODE = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String USER_ID = "user_id";
	public static final String CODE = "code";

	private final SecurityPackage securityPackage;

	@Autowired
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

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setLabel("ID");
		addAttribute(USER_ID).setNillable(false).setLabel("User ID");
		addAttribute(CODE).setNillable(false).setLabel("Recovery code");
	}
}
