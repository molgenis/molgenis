package org.molgenis.security.core.runas;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Arrays;

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
		super(new User(USER_SYSTEM, "", Arrays.asList(new SimpleGrantedAuthority(ROLE_SYSTEM))), "",
				Arrays.asList(new SimpleGrantedAuthority(ROLE_SYSTEM)));
	}
}
