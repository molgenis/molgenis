package org.molgenis.security.core.runas;

import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.molgenis.security.core.utils.SecurityUtils.ROLE_ACL_GENERAL_CHANGES;
import static org.molgenis.security.core.utils.SecurityUtils.ROLE_ACL_MODIFY_AUDITING;
import static org.molgenis.security.core.utils.SecurityUtils.ROLE_ACL_TAKE_OWNERSHIP;

/**
 * Authentication token for the SYSTEM user
 */
public class SystemSecurityToken extends UsernamePasswordAuthenticationToken
{
	private static final long serialVersionUID = 2019504169566855264L;

	public static final String ROLE_SYSTEM = "ROLE_SYSTEM";
	public static final String USER_SYSTEM = "SYSTEM";

	public SystemSecurityToken()
	{
		super(new User(USER_SYSTEM, "", singletonList(new SimpleGrantedAuthority(ROLE_SYSTEM))), "",
				Arrays.asList(new SimpleGrantedAuthority(ROLE_SYSTEM),
						new SimpleGrantedAuthority(ROLE_ACL_TAKE_OWNERSHIP),
						new SimpleGrantedAuthority(ROLE_ACL_MODIFY_AUDITING),
						new SimpleGrantedAuthority(ROLE_ACL_GENERAL_CHANGES)));
	}
}
