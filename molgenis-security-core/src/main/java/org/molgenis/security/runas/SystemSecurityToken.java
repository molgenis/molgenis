package org.molgenis.security.runas;

import java.util.Arrays;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Authentication token for the SYSTEM user
 */
public class SystemSecurityToken extends UsernamePasswordAuthenticationToken
{
	private static final long serialVersionUID = 2019504169566855264L;

	public SystemSecurityToken()
	{
		super(new User("SYSTEM", "", Arrays.asList(new SimpleGrantedAuthority("ROLE_SYSTEM"))), "", Arrays
				.asList(new SimpleGrantedAuthority("ROLE_SYSTEM")));
	}
}
