package org.molgenis.security.twofactor.services;

import org.junit.Ignore;
import org.molgenis.security.twofactor.OTPService;
import org.molgenis.security.twofactor.OTPServiceImpl;
import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { OTPServiceImplTest.Config.class })
public class OTPServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public OTPService otpService()
		{
			return new OTPServiceImpl();
		}
	}

	@Autowired
	private OTPService otpService;

	@Test(expectedExceptions = InvalidVerificationCodeException.class)
	public void testTryVerificationKeyFailed()
	{
		boolean isValid = otpService.tryVerificationCode("", "secretKey");
		assertEquals(false, isValid);
	}

	/**
	 * FIXME(SH): how do we test this properly?
	 */
	@Test
	@Ignore
	public void testTryVerificationKey()
	{
		boolean isValid = otpService.tryVerificationCode("", "secretKey");
		assertEquals(true, isValid);
	}

}
