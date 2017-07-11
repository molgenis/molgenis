package org.molgenis.data.security.acl;

import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

@Component
class SecurityIdMapper
{
	private SecurityId toSecurityId(Sid sid)
	{
		if (!(sid instanceof PrincipalSid))
		{
			throw new RuntimeException("Sid is not a PrincipalSid");
		}
		PrincipalSid principalSid = (PrincipalSid) sid;
		return SecurityId.create(principalSid.getPrincipal());
	}

	public Sid toSid(SecurityId securityId)
	{
		return new PrincipalSid(securityId.getId());
	}
}
