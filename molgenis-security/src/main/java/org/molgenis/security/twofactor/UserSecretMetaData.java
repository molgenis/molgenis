package org.molgenis.security.twofactor;

import org.molgenis.auth.SecurityPackage;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.SecurityPackage.PACKAGE_SECURITY;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

/**
 * User secret key for 2 factor authentication
 */
@Component
public class UserSecretMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "UserSecret";
	public static final String USERSECRET = PACKAGE_SECURITY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String USER_ID = "user_id";
	public static final String SECRET = "secret";
	public static final String LAST_SUCCESSFUL_AUTHENICATION = "last_successful_authentication";
	public static final String FAILED_LOGIN_ATTEMPTS = "failed_login_attempts";

	private final SecurityPackage securityPackage;

	@Autowired
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

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false).setLabel("ID");
		addAttribute(USER_ID).setNillable(false).setUnique(true).setLabel("User ID");
		addAttribute(SECRET).setNillable(false).setLabel("Secret");
		addAttribute(LAST_SUCCESSFUL_AUTHENICATION).setDataType(AttributeType.DATE_TIME)
												   .setNillable(true)
												   .setLabel("Is last successful authenticated at");
		addAttribute(FAILED_LOGIN_ATTEMPTS).setNillable(false)
										   .setDataType(AttributeType.INT)
										   .setLabel("Failed login attempts");
	}

}
