package org.molgenis.security.twofactor;

import org.jboss.aerogear.security.otp.Totp;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;

/**
 * Created by sido on 28/07/2017.
 */
public class OTPServiceImpl implements OTPService
{

	public boolean tryVerificationCode(String verificationCode, String secretKey) throws BadCredentialsException
	{
		boolean isValid;
		if (StringUtils.hasText(secretKey))
		{
			if (verificationCode == null)
			{
				throw new BadCredentialsException("2 factor authentication code is mandatory");
			}
			else
			{
				final Totp totp = new Totp(secretKey);
				if (isValidLong(verificationCode) && totp.verify(verificationCode))
				{
					isValid = true;
				}
				else
				{
					throw new BadCredentialsException("Invalid 2 factor authentication code");

				}
			}

		}
		else
		{
			throw new BadCredentialsException("2 factor authentication secret key is not available");
		}
		return isValid;
	}

	private boolean isValidLong(String code)
	{
		try
		{
			Long.parseLong(code);
		}
		catch (NumberFormatException e)
		{
			return false;
		}
		return true;
	}

}
