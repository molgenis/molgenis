package org.molgenis.security.twofactor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Holds the api token, passed by the client via a custom HTTP header
 */
public class TwoFactorAuthenticationToken extends UsernamePasswordAuthenticationToken
{
	private static final long serialVersionUID = 340142428848970352L;

	private final String verificationCode;
	private final String secretKey;
	private final String recoveryCode;

	public String getVerificationCode()
	{
		return verificationCode;
	}

	public String getSecretKey()
	{
		return secretKey;
	}

	public String getRecoveryCode()
	{
		return recoveryCode;
	}

	public TwoFactorAuthenticationToken(Object principal, Object credentials,
			Collection<? extends GrantedAuthority> authorities, String verificationCode, String secretKey,
			String recoveryCode)
	{
		super(principal, credentials, authorities);
		this.verificationCode = verificationCode;
		this.secretKey = secretKey;
		this.recoveryCode = recoveryCode;
	}

	public TwoFactorAuthenticationToken(String verificationCode, String secretKey, String recoveryCode)
	{
		super("N/A", "N/A");
		this.verificationCode = verificationCode;
		this.secretKey = secretKey;
		this.recoveryCode = recoveryCode;
	}

}
