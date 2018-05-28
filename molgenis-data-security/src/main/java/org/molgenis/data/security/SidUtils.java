package org.molgenis.data.security;

import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;

/**
 * Util class to create security identities for {@link User users} and {@link Role roles}.
 *
 * @see Sid
 */
public class SidUtils
{
	private SidUtils()
	{
	}

	public static Sid createUserSid(User user)
	{
		return createUserSid(user.getUsername());
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

	public static Sid createRoleSid(String roleName)
	{
		String roleAuthority = createRoleAuthority(roleName);
		return new GrantedAuthoritySid(roleAuthority);
	}

	public static Sid createRoleSid(Role role)
	{
		return createRoleSid(role.getName());
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
