package org.molgenis.data;

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