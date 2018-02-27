package org.molgenis.security.acl;

public interface MutableAclClassService extends AclClassService
{
	/**
	 * Creates an ACL class for the given type
	 *
	 * @param type
	 * @param idType
	 */
	void createAclClass(String type, Class<?> idType);

	void deleteAclClass(String type);
}
