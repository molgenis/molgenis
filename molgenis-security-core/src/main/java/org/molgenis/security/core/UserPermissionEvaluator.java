package org.molgenis.security.core;

import org.springframework.security.acls.model.ObjectIdentity;

import java.util.List;
import java.util.Set;

/**
 * Evaluates permissions for the currently authenticated user.
 */
public interface UserPermissionEvaluator
{
	/**
	 * Returns whether the currently authenticated user has a given {@link Permission} on the given domain object.
	 *
	 * @param objectIdentity domain object identity
	 * @param permission the {@link Permission} to check.
	 * @return <tt>true</tt> if the permission is granted, <tt>false</tt> otherwise
	 */
	//TODO: make sure the permission matches the resource type, e.g. subclass this for EntityType resource class
	boolean hasPermission(ObjectIdentity objectIdentity, Permission permission);

	/**
	 * Returns whether the currently authenticated user has a given {@link Permission} on the given domain object.
	 *
	 * @param objectIdentity domain object identity
	 * @param permissions List of{@link Permission} to check.
	 * @return <tt>true</tt> if all the permissions are granted, <tt>false</tt> otherwise
	 */
	//TODO: make sure the permission matches the resource type, e.g. subclass this for EntityType resource class
	boolean hasPermission(ObjectIdentity objectIdentity, List<Permission> permissions);

	Set<Permission> getPermissions(ObjectIdentity objectIdentity, Permission[] permissions);
}
