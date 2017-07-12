package org.molgenis.data.security.acl;

import org.molgenis.data.Entity;
import org.molgenis.security.core.Permission;

import java.util.List;

/**
 * Entity access control list retrieval.
 */
public interface EntityAclService
{
	EntityAcl readAcl(Entity entity);

	EntityAcl readAcl(Entity entity, List<SecurityId> securityIds);

	boolean isGranted(Entity entity, List<Permission> permissions, List<SecurityId> securityIds);
}
