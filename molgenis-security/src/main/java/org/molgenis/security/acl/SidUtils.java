package org.molgenis.security.acl;

import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;

/**
 * Util class to create security identities for {@link User users} and {@link Group groups}.
 *
 * @see Sid
 */
public class SidUtils
{
	private SidUtils()
	{
	}

	public static Sid createSid(User user)
	{
		return createSid(user.getUsername());
	}

	/**
	 * @deprecated use {@link #createSid(User)}
	 */
	@Deprecated
	public static Sid createSid(String username)
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

	public static Sid createAnonymousSid()
	{
		return new GrantedAuthoritySid(SecurityUtils.AUTHORITY_ANONYMOUS);
	}

	public static Sid createSid(Group group)
	{
		String groupAuthority = createGroupAuthority(group);
		return new GrantedAuthoritySid(groupAuthority);
	}

	/**
	 * @deprecated will be replaced with role retrieval based on persisted roles for a group.
	 */
	@Deprecated
	public static String createGroupAuthority(Group group)
	{
		return "ROLE" + '_' + group.getId();
	}
}
