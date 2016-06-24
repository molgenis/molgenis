/**
 * File: invengine_generate/meta/Index.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2006-01-25; 1.0.0; RA Scheltema; Creation.
 * </ul>
 */

package org.molgenis.model.elements;

// jdk
import java.io.Serializable;
import java.util.Vector;

import org.molgenis.model.MolgenisModelException;

// invengine

/**
 * This class describes a single index-field. An index makes a column (or
 * multiple columns) faster to search (binary tree idea). It can index multiple
 * columns at the same time.
 * 
 * @author RA Scheltema
 * @version 1.0.0
 */
public class Index implements Serializable
{
	private static final long serialVersionUID = 8774987474903369117L;

	// constructor(s)
	/**
	 * Default constructor, which sets the name of the index. The fields vector
	 * remains empty.
	 * 
	 * @param name
	 *            The name of the index.
	 */
	public Index(String name)
	{
		this.name = name;
		this.fields = new Vector<String>();
	}

	// access methods
	/**
	 * Returns the name of the index.
	 * 
	 * @return The name of the index.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Adds the field-name to the list of fields. The field-name needs to be
	 * unique in the list of fields, otherwise an exception is generated.
	 * 
	 * @param field
	 *            The name of the field this index indexes.
	 * @throws Exception
	 *             When the name is already present.
	 */
	public void addField(String field) throws MolgenisModelException
	{
		if (fields.contains(field))
		{
			throw new MolgenisModelException("Field with name " + field + " already in index.");
		}

		fields.add(field);
	}

	/**
	 * Returns a vector with all the field-names this index indexes.
	 * 
	 * @return Vector with all the field-names.
	 */
	public Vector<String> getFields()
	{
		return this.fields;
	}

	// Object overloads
	/**
	 * Returns a string representation of the Index.
	 * 
	 * @return The string-representation.
	 */
	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder("Index(").append(name).append(" => ");
		for (String field : fields)
		{
			strBuilder.append(field).append(", ");
		}
		strBuilder.append(')');

		return strBuilder.toString();
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * 
	 * @param obj
	 *            The reference object with which to compare.
	 * @return True if this object is the same as the obj argument, false
	 *         otherwise.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof Index)
		{
			return name.equals(((Index) obj).getName());
		}

		return false;
	}

	/**
	 * Returns a hash code value for the Field. This hash-code is used for quick
	 * searching in a vector of fields.
	 * 
	 * @return The hash-value for this field.
	 */
	@Override
	public int hashCode()
	{
		return this.name.hashCode();
	}

	// member variables
	/** The name of the index */
	String name;

	/** The field within the associated entity the index points to */
	Vector<String> fields;
}
