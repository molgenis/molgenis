package org.molgenis.omx.auth.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher
{
	private static MessageDigest m;

	public PasswordHasher() throws NoSuchAlgorithmException
	{
		m = MessageDigest.getInstance("MD5");
	}

	public String toMD5(String input)
	{
		m.reset();
		m.update(input.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1, digest);
		String hashtext = bigInt.toString(16);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while (hashtext.length() < 32)
		{
			hashtext = "0" + hashtext;
		}
		return "md5_" + hashtext;
	}
}
