package org.molgenis.data;

import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Tag;

/**
 * Package defines the structure and attributes of a Package. Attributes are unique. Other software components can use
 * this to interact with Packages and/or to configure backends and frontends, including Repository instances.
 */
public interface Package
{
	public static final String DEFAULT_PACKAGE_NAME = "base";
	public static final String PACKAGE_SEPARATOR = "_";

	/**
	 * Gets the fully qualified name of this package
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Gets the name of the package without the trailing parent packages
	 * 
	 * @return
	 */
	String getSimpleName();

	/**
	 * The description of this package
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * Gets the subpackages of this package or an empty list if this package doesnot have any subpackages.
	 * 
	 * @return
	 */
	Iterable<Package> getSubPackages();

	/**
	 * Gets the entities in this package.
	 * 
	 * @return
	 */
	Iterable<EntityMetaData> getEntityMetaDatas();

	/**
	 * Gets the parent package or null if this package does not have a parent package
	 * 
	 * @return
	 */
	Package getParent();

	/**
	 * Return a the package entity
	 * 
	 * @return
	 */
	Entity toEntity();

	/**
	 * Get the root of this package, or itsels if this is a root package
	 * 
	 * @return
	 */
	Package getRootPackage();

	/**
	 * Get all tags for this package
	 * 
	 * @return
	 */
	Iterable<Tag<Package, LabeledResource, LabeledResource>> getTags();
}
