/**
 * File: invengine_generate/meta/Field.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-12-06; 1.0.0; RA Scheltema; Creation.
 * <li> 2006-01-11; 1.0.0; RA Scheltema; Added documentation.
 * </ul>
 */

package org.molgenis.model.elements;

// jdk

import org.molgenis.model.MolgenisModelException;

import java.io.Serializable;
import java.util.List;

// invengine

/**
 * Describes a field in an entity.
 *
 * @author RA Scheltema
 * @version 1.0.0
 */
public class Parameter implements Serializable
{
	/**
	 * Description of the different types of a field.
	 */
	public enum Type
	{
		/**
		 * The type is unknown, this case should raise an exception.
		 */
		UNKNOWN("unknown"), /**
	 * The type is a simple boolean.
	 */
	BOOL("bool"), /**
	 * The type is a simple integer.
	 */
	INT("int"), /**
	 * The type is a decimal value.
	 */
	DECIMAL("decimal"), /**
	 * The type is a variable character string. More information can be
	 * found with the appropriate functions.
	 */
	VARCHAR("varchar"), /**
	 * The type is free-text. The length of the string is not defined.
	 */
	TEXT("text"), /**
	 * The type is a date-field.
	 */
	DATE("date"), /** */
	DATETIME("datetime"), /**
	 * The type of the field is user, which basically references a hidden
	 * table.
	 */
	USER("user"), /**
	 * The type of the field is file.
	 */
	FILE("file"), /** */
	ENUM("enum"),;

		// access

		/**
		 * The standard constructor, which binds a string to the
		 * enumeration-type.
		 */
		Type(String tag)
		{
			this.tag = tag;
		}

		/**
		 * With this method the enumeration-type can be found based on the given
		 * string.
		 *
		 * @param tag The string-representation of the tag.
		 * @return The enumeration-type.
		 */
		public static Type getType(String tag)
		{
			if (tag.equals(BOOL.tag))
			{
				return BOOL;
			}
			else if (tag.equals(INT.tag))
			{
				return INT;
			}
			else if (tag.equals(DECIMAL.tag))
			{
				return DECIMAL;
			}
			else if (tag.equals(VARCHAR.tag))
			{
				return VARCHAR;
			}
			else if (tag.equals(TEXT.tag))
			{
				return TEXT;
			}
			else if (tag.equals(DATE.tag))
			{
				return DATE;
			}
			else if (tag.equals(DATETIME.tag))
			{
				return DATETIME;
			}
			else if (tag.equals(USER.tag))
			{
				return USER;
			}
			else if (tag.equals(FILE.tag))
			{
				return FILE;
			}
			else if (tag.equals(ENUM.tag))
			{
				return ENUM;
			}
			else
			{
				return UNKNOWN;
			}
		}

		/**
		 * The string-representation of the enumeration-type.
		 */
		public final String tag;
	}

	/**
	 * Fixed value used for determining the not-set value for the varchar.
	 */
	public static final int LENGTH_NOT_SET = 0;

	// constructor(s)

	/**
	 * Constructor specifically meant for constructing a return-type. This
	 * avoids the need for an additional class describing basically the same
	 * thing. The properties: name, label and default_value are set to null and
	 * nillable to false.
	 *
	 * @see #Parameter(Method, Type, String, String, boolean, String)
	 */
	public Parameter(Method parent, Type type)
	{
		this(parent, type, null, null, false, null);
	}

	/**
	 * Standard constructor, which sets all the common variables for a field.
	 * Extra fields can be set with the appropriate access methods.
	 *
	 * @param type     The type of the field.
	 * @param name     The name of the field, which needs to be unique for the
	 *                 entity.
	 * @param label    The label of the field, which is used for the user interface.
	 * @param nillable Indicates whether this field can have the value NULL in the
	 *                 database.
	 */
	public Parameter(Method parent, Type type, String name, String label, boolean nillable, String default_value)
	{
		this.parent = parent;

		// global
		this.type = type;

		this.name = name;
		this.label = label;
		this.nillable = nillable;
		this.default_value = default_value;

		this.description = "";

		if (this.label == null || this.label.isEmpty()) this.label = this.name;

		// varchar
		this.varchar_length = LENGTH_NOT_SET;

		//
		this.user_data = null;
	}

	// global access methods

	/**
	 *
	 */
	@Deprecated
	public Method getParent()
	{
		return parent;
	}

	public Method getEntity()
	{
		return parent;
	}

	/**
	 * This method returns the type of this field.
	 *
	 * @return The type of this field.
	 */
	public Type getType()
	{
		return this.type;
	}

	/**
	 * @param type
	 */
	public void setType(Type type)
	{
		this.type = type;
	}

	/**
	 * This method returns the name of this field.
	 *
	 * @return The name of this field.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * This method returns the label of this field.
	 *
	 * @return The label of this field.
	 */
	public String getLabel()
	{
		return this.label;
	}

	/**
	 * Returns whether this field can be NULL in the database.
	 *
	 * @return True when this field can be NULL, false otherwise.
	 */
	public boolean isNillable()
	{
		return this.nillable;
	}

	/**
	 * Returns the value the database should set for the field when there is no
	 * value set.
	 *
	 * @return The default-value.
	 */
	public String getDefaultValue()
	{
		return this.default_value;
	}

	/**
	 * Returns the description of the entity.
	 *
	 * @return The description.
	 */
	public String getDescription()
	{
		return this.description;
	}

	/**
	 * Sets the description of this entity.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	// enum access methods

	/**
	 *
	 */
	public void setEnumOptions(List<String> options) throws MolgenisModelException
	{
		if (this.type != Type.ENUM)
		{
			throw new MolgenisModelException("Field is not a ENUM, so options cannot be set.");
		}
		if (options.size() == 0)
		{
			throw new MolgenisModelException("Enum must have at least one option");
		}

		this.enum_options = options;
	}

	/**
	 *
	 */
	public List<String> getEnumOptions() throws MolgenisModelException
	{
		if (this.type != Type.ENUM)
		{
			throw new MolgenisModelException("Field is not a ENUM, so options cannot be set.");
		}

		return this.enum_options;
	}

	// varchar access methods

	/**
	 * When this field is of type Type.VARCHAR, this method sets the maximum
	 * length the varchar can be. When this field is not of type Type.VARCHAR,
	 * this method raises an exception.
	 *
	 * @param length The maximum length the varchar field can be.
	 * @throws Exception When the field is not of type Type.VARCHAR.
	 */
	public void setVarCharLength(int length) throws Exception
	{
		if (this.type != Type.VARCHAR)
		{
			throw new Exception("Field is not a VARCHAR, so length cannot be set.");
		}

		this.varchar_length = length;
	}

	/**
	 * When this field is of type Type.VARCHAR, this method returns the maximum
	 * length the varchar can be. When this field is not of type Type.VARCHAR,
	 * this method raises an exception.
	 *
	 * @return The maximum length the varchar field can be.
	 * @throws Exception When the field is not of type Type.VARCHAR.
	 */
	public int getVarCharLength() throws Exception
	{
		if (this.type != Type.VARCHAR)
		{
			throw new Exception("Field is not a VARCHAR, so length cannot be retrieved.");
		}

		return this.varchar_length;
	}

	//

	/**
	 *
	 */
	public void setUserData(Object obj)
	{
		user_data = obj;
	}

	/**
	 *
	 */
	public Object getUserData()
	{
		return user_data;
	}

	// Object overloads

	/**
	 * Returns a string representation of the Field.
	 *
	 * @return The string-representation.
	 */
	@Override
	public String toString()
	{
		String str = "Parameter(";

		// name/label
		str += name;

		// type
		str += ", " + type.tag;
		if (type == Parameter.Type.VARCHAR) str += "[" + varchar_length + "]";

		// settings
		str += ", nillable=" + nillable;

		// default
		str += ", default=" + default_value;

		// closure
		str += ")";

		return str;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 *
	 * @param obj The reference object with which to compare.
	 * @return True if this object is the same as the obj argument, false
	 * otherwise.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof Parameter)
		{
			return name.equals(((Parameter) obj).getName());
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
	/** */
	private Method parent;

	/**
	 * The type of this field.
	 */
	private Type type;

	/**
	 * The name of this field, which needs to be unique for the associated
	 * entity.
	 */
	private String name;

	/**
	 * The label of this field, which is used for the user interface.
	 */
	private String label;

	/**
	 * Whether this field can be NULL in the database.
	 */
	private boolean nillable;

	/**
	 * The string that should be set as the default value (is passed to the
	 * database ...)
	 */
	private String default_value;

	/**
	 * A short description of this field.
	 */
	private String description;

	/**
	 * When this field a of type Type.ENUM, this vector contains the options
	 */
	private List<String> enum_options;

	/**
	 * When this field is of type Type.VARCHAR, this indicates the maximum
	 * length of the string.
	 */
	private int varchar_length;

	/**
	 * Contains a pointer to some user-data.
	 */
	private Object user_data;

	/**
	 * Used for serialization purposes.
	 */
	private static final long serialVersionUID = -1879739243713730190L;
}
