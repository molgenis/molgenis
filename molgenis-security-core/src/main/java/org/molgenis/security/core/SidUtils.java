package org.molgenis.security.core;

import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;

/**
 * Util class to create security identities for users and roles.
 *
 * @see Sid
 */
public class SidUtils
{
	private SidUtils()
	{
	}

	public static Sid createUserSid(String username)
	{
		if (username.equals(SecurityUtils.ANONYMOUS_USERNAME))
		{
			return createAnonymousSid();
		}
		else
		{
			return new PrincipalSid(username);
		}
	}

	public static Sid createRoleSid(String rolename)
	{
		String roleAuthority = createRoleAuthority(rolename);
		return new GrantedAuthoritySid(roleAuthority);
	}

	public static String createRoleAuthority(String roleName)
	{
		return "ROLE" + '_' + roleName;
	}

	private static Sid createAnonymousSid()
	{
		return new GrantedAuthoritySid(SecurityUtils.AUTHORITY_ANONYMOUS);
	}
}
