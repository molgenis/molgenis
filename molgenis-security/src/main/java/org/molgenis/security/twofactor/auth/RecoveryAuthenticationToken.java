package org.molgenis.security.twofactor.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

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
}
