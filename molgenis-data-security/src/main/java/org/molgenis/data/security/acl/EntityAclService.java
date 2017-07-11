package org.molgenis.data.security.acl;

import org.molgenis.data.Entity;

/**
 * Entity access control list retrieval.
 */
public interface EntityAclService
{
	EntityAcl readAcl(Entity entity);
}
