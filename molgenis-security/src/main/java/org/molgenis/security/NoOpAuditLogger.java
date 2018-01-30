package org.molgenis.security;

import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.model.AccessControlEntry;

/**
 * A no operation {@link AuditLogger} suitable for disabling audit logging.
 */
public class NoOpAuditLogger implements AuditLogger
{
	@Override
	public void logIfNeeded(boolean granted, AccessControlEntry ace)
	{
		// no operation
	}
}
