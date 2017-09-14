package org.molgenis.security.twofactor.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Objects;

public class RecoveryAuthenticationToken extends UsernamePasswordAuthenticationToken
{
	private static final long serialVersionUID = 1L;

	private final String recoveryCode;

	public RecoveryAuthenticationToken(Object principal, Object credentials,
			Collection<? extends GrantedAuthority> authorities, String recoveryCode)
	{
		super(principal, credentials, authorities);
		this.recoveryCode = recoveryCode;
	}

	public RecoveryAuthenticationToken(String recoveryCode)
	{
		super("N/A", "N/A");
		this.recoveryCode = recoveryCode;
	}

	public String getRecoveryCode()
	{
		return recoveryCode;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		RecoveryAuthenticationToken that = (RecoveryAuthenticationToken) o;
		return Objects.equals(recoveryCode, that.recoveryCode);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), recoveryCode);
	}
}
