
package org.molgenis.integrationtest.data;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecuritySupportService
{
	public void logout()
	{
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
