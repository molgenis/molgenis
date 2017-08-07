package org.molgenis.security.google;

import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author tommy
 */
@Service
public class GoogleAuthenticatorServiceImpl implements GoogleAuthenticatorService
{
	@Override
	public String getGoogleAuthenticatorURI(String secretKey) throws IllegalStateException
	{
		String normalizedBase32Key = secretKey.replace(" ", "").toUpperCase();
		try
		{
			return String.format("otpauth://totp/%s?secret=%s&issuer=%s",
					URLEncoder.encode("molgenis" + ":" + "admin", "UTF-8").replace("+", "%20"),
					URLEncoder.encode(normalizedBase32Key, "UTF-8").replace("+", "%20"),
					URLEncoder.encode("molgenis", "UTF-8").replace("+", "%20"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalStateException(e);
		}
	}

}
