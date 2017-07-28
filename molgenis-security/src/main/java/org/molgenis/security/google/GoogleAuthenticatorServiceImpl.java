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
	public String getGoogleAuthenticatorURI(String secretKey)
	{
		String normalizedBase32Key = secretKey.replace(" ", "").toUpperCase();
		String user = "";
		try
		{
			return "otpauth://totp/" + URLEncoder.encode("molgenis" + ":" + "admin", "UTF-8").replace("+", "%20")
					+ "?secret=" + URLEncoder.encode(normalizedBase32Key, "UTF-8").replace("+", "%20") + "&issuer="
					+ URLEncoder.encode("molgenis", "UTF-8").replace("+", "%20");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalStateException(e);
		}
	}

}
