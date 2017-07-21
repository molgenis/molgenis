package org.molgenis.security.acl;

import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.model.*;
import org.springframework.util.Assert;

import java.util.List;

/**
 * A small modification from Spring's
 * {@link org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy}
 * that performs bit-wise permission comparison.
 * Useful if you want to be able to assign multiple permissions to a SID using a single ACE
 *
 * @see {https://gist.github.com/oliverfernandez/36846fcdc03696a7b829}
 */
public class BitMaskPermissionGrantingStrategy implements PermissionGrantingStrategy
{
	private final transient AuditLogger auditLogger;

	public BitMaskPermissionGrantingStrategy(AuditLogger auditLogger)
	{
		Assert.notNull(auditLogger, "auditLogger cannot be null");
		this.auditLogger = auditLogger;
	}

	@Override
	public boolean isGranted(Acl acl, List<Permission> permission, List<Sid> sids, boolean administrativeMode)
	{
		final List<AccessControlEntry> aces = acl.getEntries();

		AccessControlEntry firstRejection = null;

		for (Permission p : permission)
		{
			for (Sid sid : sids)
			{
				// Attempt to find exact match for this permission mask and SID
				boolean scanNextSid = true;

				for (AccessControlEntry ace : aces)
				{

					//Bit-wise comparison
					if (containsPermission(ace.getPermission().getMask(), p.getMask()) && ace.getSid().equals(sid))
					{
						// Found a matching ACE, so its authorization decision will prevail
						if (ace.isGranting())
						{
							// Success
							if (!administrativeMode)
							{
								auditLogger.logIfNeeded(true, ace);
							}

							return true;
						}

						// Failure for this permission, so stop search
						// We will see if they have a different permission
						// (this permission is 100% rejected for this SID)
						if (firstRejection == null)
						{
							// Store first rejection for auditing reasons
							firstRejection = ace;
						}

						scanNextSid = false; // helps break the loop

						break; // exit aces loop
					}
				}

				if (!scanNextSid)
				{
					break; // exit SID for loop (now try next permission)
				}
			}
		}

		if (firstRejection != null)
		{
			// We found an ACE to reject the request at this point, as no
			// other ACEs were found that granted a different permission
			if (!administrativeMode)
			{
				auditLogger.logIfNeeded(false, firstRejection);
			}

			return false;
		}

		// No matches have been found so far
		if (acl.isEntriesInheriting() && (acl.getParentAcl() != null))
		{
			// We have a parent, so let them try to find a matching ACE
			return acl.getParentAcl().isGranted(permission, sids, administrativeMode);
		}
		else
		{
			// We either have no parent, or we're the uppermost parent
			throw new NotFoundException("Unable to locate a matching ACE for passed permissions and SIDs");
		}
	}

	public boolean containsPermission(int mask, int permission)
	{
		return (mask & permission) == permission;
	}
}
