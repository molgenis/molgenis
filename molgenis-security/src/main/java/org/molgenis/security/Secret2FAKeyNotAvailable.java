package org.molgenis.security;

import org.springframework.security.core.AuthenticationException;

/**
 * Created by sido on 25/07/2017.
 */
public class Secret2FAKeyNotAvailable extends AuthenticationException
{
	public Secret2FAKeyNotAvailable(String message) {
		super(message);
	}
}
