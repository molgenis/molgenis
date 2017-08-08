package org.molgenis.security.twofactor;

import org.jboss.aerogear.security.otp.Totp;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
public class OTPServiceImpl implements OTPService
{

	private final static Logger LOG = LoggerFactory.getLogger(OTPServiceImpl.class);

	private final AppSettings appSettings;

	@Autowired
	public OTPServiceImpl(AppSettings appSettings)
	{
		this.appSettings = appSettings;
	}

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

	@Override
	public String getAuthenticatorURI(String secretKey) throws IllegalStateException
	{
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		String appName = appSettings.getTitle() == null ? "MOLGENIS" : appSettings.getTitle();
		if (userDetails == null)
		{
			String errorMessage = "User is not available";
			LOG.error(errorMessage);
			throw new IllegalStateException();
		}
		String userName = userDetails.getUsername();
		String normalizedBase32Key = secretKey.replace(" ", "").toUpperCase();
		try
		{
			return String.format("otpauth://totp/%s?secret=%s&issuer=%s",
					URLEncoder.encode(appName + ":" + userName, "UTF-8").replace("+", "%20"),
					URLEncoder.encode(normalizedBase32Key, "UTF-8").replace("+", "%20"),
					URLEncoder.encode(appName, "UTF-8").replace("+", "%20"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalStateException(e);
		}
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
