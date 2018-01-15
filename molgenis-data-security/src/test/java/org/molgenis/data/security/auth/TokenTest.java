package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { TokenFactory.class, TokenMetaData.class, SecurityPackage.class, UserMetaData.class })
public class TokenTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private TokenFactory tokenFactory;

	private Token token;

	@BeforeMethod
	public void setup()
	{
		token = tokenFactory.create();
	}

	@Test
	public void testIsExpiredNoExpirationDate() throws Exception
	{
		assertFalse(token.isExpired());
	}

	@Test
	public void testIsExpiredExpirationDateInFuture() throws Exception
	{
		token.setExpirationDate(now().plus(1, MINUTES));
		assertFalse(token.isExpired());
	}

	@Test
	public void testIsExpiredExpirationDateInPast() throws Exception
	{
		token.setExpirationDate(now().minus(1, SECONDS));
		assertTrue(token.isExpired());
	}

}