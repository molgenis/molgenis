package org.molgenis.model.elements;

// jdk
import java.util.List;
import java.util.Vector;

import org.molgenis.model.MolgenisModelException;

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
	public String getName();

	/**
	 * 
	 */
	public String getLabel();

	/**
	 * @throws MolgenisModelException
	 * 
	 */
	public List<Field> getFields() throws MolgenisModelException;

	/**
	 * 
	 */
	public Vector<String> getParents();

	// small utility methods
	/**
	 * 
	 */
	public boolean hasXRefs();

	// public List<Field> getAllFields(Record e, String type);
}
