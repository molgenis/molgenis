package org.molgenis.security.acl;

import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.User;
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
		return new PrincipalSid(user.getId());
	}

	public static Sid createSid(Group group)
	{
		return new GrantedAuthoritySid("ROLE" + '_' + group.getId());
	}
}
