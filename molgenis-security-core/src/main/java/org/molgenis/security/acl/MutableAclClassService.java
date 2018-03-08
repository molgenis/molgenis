package org.molgenis.security.acl;

import org.springframework.security.acls.model.Acl;

/**
 * Provides support for creating and deleting {@link Acl} classes.
 */
public interface MutableAclClassService extends AclClassService
{
	/**
	 * Creates an ACL class for the given type.
	 *
	 * @param type   domain object type
	 * @param idType domain object identifier type (e.g. String or Long)
	 */
	void createAclClass(String type, Class<?> idType);

	/**
	 * Deletes an ACL class and associated ACLs and ACEs for the given type.
	 *
	 * @param type domain object type
	 */
	void deleteAclClass(String type);
}
