package org.molgenis.bootstrap.populate;

import com.google.common.collect.Multimap;
import org.molgenis.util.Pair;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

/**
 * Registry of application permissions used to bootstrap application.
 * Classes implementing this interface must be annotated with as
 * {@link org.springframework.stereotype.Component Component}.
 */
public interface PermissionRegistry
{
	/**
	 * Returns the permissions to populate
	 *
	 * @return permissions to populate
	 */
	Multimap<ObjectIdentity, Pair<Permission, Sid>> getPermissions();
}
