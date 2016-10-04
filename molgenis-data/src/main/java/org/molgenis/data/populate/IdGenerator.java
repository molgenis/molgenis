package org.molgenis.data.populate;

/**
 * Unique id generator
 */
public interface IdGenerator
{
	/**
	 * Generate a unique id
	 *
	 * @return a unique string
	 */
	String generateId();
}