package org.molgenis.security;

import org.molgenis.omx.auth.util.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MolgenisPasswordEncoder implements PasswordEncoder
{
	@Override
	public String encode(CharSequence rawPassword)
	{
		return new PasswordHasher().toMD5(rawPassword.toString());
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword)
	{
		return new PasswordHasher().toMD5(rawPassword.toString()).equals(encodedPassword);
	}
}
