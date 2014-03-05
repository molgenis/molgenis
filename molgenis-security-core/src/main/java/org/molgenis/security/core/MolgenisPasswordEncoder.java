package org.molgenis.security.core;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MolgenisPasswordEncoder implements PasswordEncoder
{
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	public MolgenisPasswordEncoder(BCryptPasswordEncoder bCryptPasswordEncoder)
	{
		if (bCryptPasswordEncoder == null) throw new IllegalArgumentException("BCrypt password encoder is null");
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}

	@Override
	public String encode(CharSequence rawPassword)
	{
		return bCryptPasswordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword)
	{
		return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
	}
}
