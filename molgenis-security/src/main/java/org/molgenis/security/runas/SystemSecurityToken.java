package org.molgenis.security.runas;

import java.util.Arrays;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Authentication token for the SYSTEM user
 */
public class SystemSecurityToken extends AbstractAuthenticationToken
{
	private static final long serialVersionUID = 2019504169566855264L;

	public SystemSecurityToken()
	{
		super(Arrays.asList(new SimpleGrantedAuthority("ROLE_SYSTEM")));
	}

	@Override
	public Object getCredentials()
	{
		return "";
	}

	@Override
	public Object getPrincipal()
	{
		return new User("SYSTEM", "", getAuthorities());
	}

}
