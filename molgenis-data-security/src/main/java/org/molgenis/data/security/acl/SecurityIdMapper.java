package org.molgenis.data.security.acl;

import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

@Component
class SecurityIdMapper
{
	SecurityId toSecurityId(Sid sid)
	{
		if (!(sid instanceof PrincipalSid))
		{
			throw new RuntimeException("Sid is not a PrincipalSid");
		}
		String principal = ((PrincipalSid) sid).getPrincipal();

		SecurityId.Type type = getSecurityIdType(principal);
		String id = principal.substring(2);
		return SecurityId.create(id, type);
	}

	private SecurityId.Type getSecurityIdType(String principal)
	{
		switch (principal.charAt(0))
		{
			case 'U':
				return SecurityId.Type.USER;
			case 'G':
				return SecurityId.Type.GROUP;
			default:
				throw new IllegalArgumentException(String.format("Unknown principal prefix '%s'", principal));
		}
	}

	Sid toSid(SecurityId securityId)
	{
		char principalPrefix = getPrincipalPrefix(securityId);
		return new PrincipalSid(principalPrefix + '-' + securityId.getId());
	}

	private char getPrincipalPrefix(SecurityId securityId)
	{
		switch (securityId.getType())
		{
			case USER:
				return 'U';
			case GROUP:
				return 'G';
			default:
				throw new IllegalArgumentException(String.format("Unknown security id type '%s'", securityId));
		}
	}
}
