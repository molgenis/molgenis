// File:		util/FieldAssign.java
// Copyright:	GBIC 2005, all rights reserved
//
// Changelog:
// 2005-11-17; 1.0.0; RA Scheltema;
//	Creation.
//

package org.molgenis.util.cmdline;

// jdk
import java.util.*;
import java.lang.reflect.*;

/**
 * ...
 * 
 * @author RA Scheltema
 * @version 1.0.0
 * 
 *          DEPRECATED AS OF April 15th 2011. Not longer needed due to assign()
 *          in org.molgenis.util.cmdline.CmdLineParser.
 */
@Deprecated
public class FieldAssign
{
	// constructor / destructor
	/**
	 * ...
	 * 
	 * @param field
	 *            ...
	 * @throws NullPointerException
	 *             When the field f is null.
	 * @throws CmdLineException
	 */
	public FieldAssign(final Field field) throws CmdLineException
	{
		if (field == null)
		{
			throw new CmdLineException("Parameter field cannot be null");
		}

		this.field = field;
	}

	// public methods
	/**
	 * This method applies the given value to the field in the given object. It
	 * checks whether the field is a primitive and converts the value
	 * accordingly.
	 * 
	 * @param obj
	 *            The object to set.
	 * @param value
	 *            The value to set the obj to.
	 * @throws NullPointerException
	 *             When obj or value are null.
	 * @throws IllegalAccessException
	 *             When the obj cannot be set to the given value.
	 * @throws CmdLineException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings(
	{ "unchecked" })
	public void assign(final Object obj, final Object value) throws IllegalAccessException, CmdLineException,
			IllegalArgumentException, InstantiationException
	{
		if (obj == null || value == null)
		{
			throw new CmdLineException("The parameters obj and value cannot be null.");
		}

		final Class<?> c = field.getType();
		if (c.isPrimitive())
		{
			String name = c.getSimpleName();
			if ("int".equals(name))
			{
				field.setInt(obj, Integer.valueOf(value.toString()));
			}
			else if ("short".equals(name))
			{
				field.setLong(obj, Short.valueOf(value.toString()));
			}
			else if ("long".equals(name))
			{
				field.setLong(obj, Long.valueOf(value.toString()));
			}
			else if ("boolean".equals(name))
			{
				field.setBoolean(obj, Boolean.valueOf(value.toString()));
			}
			else if ("float".equals(name))
			{
				field.setFloat(obj, Float.valueOf(value.toString()));
			}
			else if ("double".equals(name))
			{
				field.setDouble(obj, Double.valueOf(value.toString()));
			}
			else if ("char".equals(name))
			{
				field.setChar(obj, value.toString().charAt(0));
			}
			else if ("byte".equals(name))
			{
				field.setByte(obj, Byte.valueOf(value.toString()));
			}

		}
		else
		{
			// TODO this is a crappy construction ...
			// TODO we're only able to do Strings in the collection ...
			// System.out.println(field.getType().getSimpleName());
			//
			// Class iclass = field.getType().getInterfaces()[0];
			// if(field.getType(). List) {
			//
			// }

			if (Collection.class.isInstance(field.getType().newInstance()))
			{
				Collection<String> collection = (Collection<String>) field.get(obj);
				for (String v : value.toString().split(","))
				{
					collection.add(v);
				}
			}
			else
			{
				field.set(obj, value);
			}
		}
	}

	// private members
	/** Reference to the field. */
	private Field field;
}
