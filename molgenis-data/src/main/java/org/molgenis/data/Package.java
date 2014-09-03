package org.molgenis.data;

/**
 * Represents a package of EntityMetaData 
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
