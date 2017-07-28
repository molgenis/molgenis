package org.molgenis.security.twofactor;

import org.molgenis.security.google.GoogleAuthenticatorService;
import org.molgenis.security.google.GoogleAuthenticatorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { GoogleAuthenticatorImplTest.Config.class })
public class GoogleAuthenticatorImplTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public GoogleAuthenticatorService otpService()
		{
			return new GoogleAuthenticatorServiceImpl();
		}
	}

	@Autowired
	private GoogleAuthenticatorService googleAuthenticatorService;

	@Test
	public void getGoogleAuthenticatorURITest()
	{
		String key = googleAuthenticatorService.getGoogleAuthenticatorURI("testKey");
		assertEquals("otpauth://totp/molgenis%3Aadmin?secret=TESTKEY&issuer=molgenis", key);
	}

}
