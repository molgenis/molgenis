package org.molgenis.model.elements;

// jdk

import org.molgenis.model.MolgenisModelException;

import java.util.List;
import java.util.Vector;

// invengine

/**
 * This interface describes the functionality for a Record. A record is defined
 * as a single or a number of tables, which can yield data. This means that ...
 *
 * @author RA Scheltema
 */
public interface Record
{
	// access

	/**
	 *
	 */
	String getName();

	/**
	 *
	 */
	String getLabel();

	/**
	 * @throws MolgenisModelException
	 */
	List<Field> getFields() throws MolgenisModelException;

	/**
	 *
	 */
	Vector<String> getParents();

	// small utility methods

	/**
	 *
	 */
	boolean hasXRefs();

	// public List<Field> getAllFields(Record e, String type);
}
