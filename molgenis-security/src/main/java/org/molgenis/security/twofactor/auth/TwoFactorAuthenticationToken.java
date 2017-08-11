package org.molgenis.security.twofactor.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Holds the api token, passed by the client via a custom HTTP header
 */
public class TwoFactorAuthenticationToken extends UsernamePasswordAuthenticationToken
{
	private static final long serialVersionUID = 1L;

	private final String verificationCode;
	private final String secretKey;

	public TwoFactorAuthenticationToken(Object principal, Object credentials,
			Collection<? extends GrantedAuthority> authorities, String verificationCode, String secretKey)
	{
		super(principal, credentials, authorities);
		this.verificationCode = verificationCode;
		this.secretKey = secretKey;
	}

	public TwoFactorAuthenticationToken(String verificationCode, String secretKey)
	{
		super("N/A", "N/A");
		this.verificationCode = verificationCode;
		this.secretKey = secretKey;
	}

	public String getVerificationCode()
	{
		return verificationCode;
	}

	public String getSecretKey()
	{
		return secretKey;
	}

}
