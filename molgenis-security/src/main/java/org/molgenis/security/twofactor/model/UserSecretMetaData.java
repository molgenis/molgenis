package org.molgenis.security.twofactor.model;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.security.auth.SecurityPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.security.auth.SecurityPackage.PACKAGE_SECURITY;

/**
 * User secret key for 2 factor authentication
 */
@Component
public class UserSecretMetaData extends SystemEntityType
{
	public static final String ID = "id";
	public static final String USER_ID = "userId";
	public static final String SECRET = "secret";
	public static final String LAST_FAILED_AUTHENICATION = "last_failed_authentication";
	public static final String FAILED_LOGIN_ATTEMPTS = "failed_login_attempts";
	private static final String SIMPLE_NAME = "UserSecret";
	public static final String USER_SECRET = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;
	private final SecurityPackage securityPackage;

	public UserSecretMetaData(SecurityPackage securityPackage)
	{
		super(SIMPLE_NAME, PACKAGE_SECURITY);
		this.securityPackage = requireNonNull(securityPackage);
	}

	@Override
	public void init()
	{
		setLabel("User Secret");
		setPackage(securityPackage);

		setDescription("Secret that is used to authenticate user with 2 factor authentication");

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setLabel("Identifer");
		addAttribute(USER_ID).setNillable(false).setUnique(true).setLabel("User identifier");
		addAttribute(SECRET).setNillable(false).setLabel("Secret");
		addAttribute(LAST_FAILED_AUTHENICATION).setDataType(AttributeType.DATE_TIME)
											   .setNillable(true)
											   .setLabel("Is last successful authenticated at");
		addAttribute(FAILED_LOGIN_ATTEMPTS).setDefaultValue("0")
										   .setNillable(false)
										   .setDataType(AttributeType.INT)
										   .setLabel("Failed login attempts");
	}

}
