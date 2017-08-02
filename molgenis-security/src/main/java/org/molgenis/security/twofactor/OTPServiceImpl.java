package org.molgenis.security.twofactor;

import org.jboss.aerogear.security.otp.Totp;
import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OTPServiceImpl implements OTPService
{

	@Override
	public boolean tryVerificationCode(String verificationCode, String secretKey)
			throws InvalidVerificationCodeException
	{
		boolean isValid;
		if (StringUtils.hasText(secretKey))
		{
			if (verificationCode == null)
			{
				throw new InvalidVerificationCodeException("Verificationcode is mandatory");
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
					throw new InvalidVerificationCodeException("Invalid verificationcode entered");

				}
			}

		}
		else
		{
			throw new InvalidVerificationCodeException("2 factor authentication secret key is not available");
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
