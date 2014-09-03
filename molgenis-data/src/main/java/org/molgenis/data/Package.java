package org.molgenis.data;

/**
 * Package defines the structure and attributes of a Package. Attributes are unique. Other software components
 * can use this to interact with Packages and/or to configure backends and frontends, including Repository instances.
 */
public interface Package
{
	/**
	 * Get names of all the entities in this source
	 */
	Iterable<String> getEntityNames();
	
	/**
	 * Gets the name of this package
	 * @return
	 */
	String getName();

	/**
	 * The description of this package
	 * @return
	 */
	String getDescription();
}
