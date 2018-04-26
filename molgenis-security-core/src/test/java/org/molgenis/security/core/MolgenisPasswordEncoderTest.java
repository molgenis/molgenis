package org.molgenis.security.core;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class MolgenisPasswordEncoderTest
{

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisPasswordEncoder()
	{
		new MolgenisPasswordEncoder(null);
	}

	@Test
	public void encode()
	{
		String password = "password";
		String encodedPassword = "encoded-password";
		BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
		when(bCryptPasswordEncoder.encode(password)).thenReturn(encodedPassword);
		assertEquals(new MolgenisPasswordEncoder(bCryptPasswordEncoder).encode(password), encodedPassword);
	}

	@Test
	public void matches()
	{
		String password = "password";
		String encodedPassword = "encoded-password";
		BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
		when(bCryptPasswordEncoder.matches(password, encodedPassword)).thenReturn(true);
		assertTrue(new MolgenisPasswordEncoder(bCryptPasswordEncoder).matches(password, encodedPassword));
	}

	@Test
	public void matches_noMatch()
	{
		String password = "password";
		String encodedPassword = "encoded-password";
		BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
		when(bCryptPasswordEncoder.matches(password, encodedPassword)).thenReturn(true);
		assertFalse(new MolgenisPasswordEncoder(bCryptPasswordEncoder).matches("invalid-password", encodedPassword));
	}
}
