package org.molgenis.security.twofactor.service;

import org.jboss.aerogear.security.otp.Totp;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.molgenis.settings.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
public class OtpServiceImpl implements OtpService
{

	private final static Logger LOG = LoggerFactory.getLogger(OtpServiceImpl.class);

	private final AppSettings appSettings;

	public OtpServiceImpl(AppSettings appSettings)
	{
		this.appSettings = appSettings;
	}

	@Override
	public boolean tryVerificationCode(String verificationCode, String secretKey)
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
					throw new InvalidVerificationCodeException("Invalid verification code entered");

				}
			}

		}
		else
		{
			throw new InvalidVerificationCodeException("Two factor authentication secret key is not available");
		}
		return isValid;
	}

	@Override
	public String getAuthenticatorURI(String secretKey)
	{
		String userName = SecurityUtils.getCurrentUsername();

		String appName = appSettings.getTitle();
		if (userName == null)
		{
			String errorMessage = "User is not available";
			LOG.error(errorMessage);
			throw new IllegalStateException(errorMessage);
		}
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
