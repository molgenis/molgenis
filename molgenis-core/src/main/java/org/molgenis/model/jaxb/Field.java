package org.molgenis.model.jaxb;

import java.sql.Types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

@XmlAccessorType(XmlAccessType.FIELD)
public class Field
{
	private static final Logger logger = Logger.getLogger(Field.class.getSimpleName());

	// jaxb orders properties in reverse order :-s
	@XmlAttribute(name = "mref_remoteid")
	private String mref_remoteid;

	@XmlAttribute(name = "mref_localid")
	private String mref_localid;

	@XmlAttribute(name = "mref_name")
	private String mref_name;

	@XmlAttribute(name = "xref_field")
	private String xref_field;

	@XmlAttribute(name = "xref_entity")
	private String xref_entity;

	@XmlAttribute(name = "xref_name")
	private String xref_name;

	@XmlAttribute(name = "xref_label")
	private String xref_label;

	@XmlAttribute(name = "unique")
	private Boolean unique;

	@XmlAttribute(name = "length")
	private Integer length;

	@XmlAttribute(name = "default")
	private String defaultValue;

	@XmlAttribute(name = "description")
	private String description;

	@XmlAttribute(name = "enum_options")
	private String enum_options;

	@XmlAttribute(name = "auto")
	private Boolean auto = null;

	@XmlAttribute(name = "nillable")
	private Boolean nillable = null;

	@XmlAttribute(name = "readonly")
	private Boolean readonly = null;

	@XmlAttribute(name = "type")
	private Type type;

	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "label")
	private String label;

	/**
	 * Description of the different types of a field.
	 */
	public static enum Type
	{
		/** The type is unknown, this case should raise an exception. */
		@XmlEnumValue("unknown")
		UNKNOWN("unknown", ""), @XmlEnumValue("autoid")
		AUTOID("autoid", "%s"),
		/** The type is a simple boolean. */
		@XmlEnumValue("bool")
		BOOL("bool", "%d"),
		/** The type is a simple integer. */
		@XmlEnumValue("int")
		INT("int", "%d"),
		/** The type is a decimal value. */
		@XmlEnumValue("long")
		LONG("long", "%d"),
		/** The type is a decimal value. */
		@XmlEnumValue("decimal")
		DECIMAL("decimal", "%.20g"),
		/**
		 * The type is a variable character string. More information can be
		 * found with the appropriate functions.
		 */
		@XmlEnumValue("string")
		STRING("string", "%s"),
		/** fixed length */
		@XmlEnumValue("char")
		CHAR("char", "%s"),
		/** The type is free-text. The length of the string is not defined. */
		@XmlEnumValue("text")
		TEXT("text", "%s"),
		/** The type is a date-field. */
		@XmlEnumValue("date")
		DATE("date", "%s"),
		/** */
		@XmlEnumValue("datetime")
		DATETIME("datetime", "%s"),
		/**
		 * The type of the field is user, which basically references a hidden
		 * table.
		 */
		@XmlEnumValue("user")
		USER("user", "%s"),
		/** The type of the field is file. */
		@XmlEnumValue("file")
		FILE("file", "%s"),
		/** */
		@XmlEnumValue("enum")
		ENUM("enum", "%s"),
		/** Reference to another table, which can contain only 1 value. */
		@XmlEnumValue("xref")
		XREF_SINGLE("xref", ""),
		/** Reference to another table, which can contain multiple values. */
		@XmlEnumValue("mref")
		XREF_MULTIPLE("mref", ""), @XmlEnumValue("has")
		HAS_SINGLE("has", ""), @XmlEnumValue("has_many")
		HAS_MULTIPLE("has_many", ""),
		/** hyperlink */
		@XmlEnumValue("hyperlink")
		HYPERLINK("hyperlink", "%s"),
		/** List of values */
		@XmlEnumValue("list")
		LIST("list", "%s");

		// access
		/**
		 * The standard constructor, which binds a string to the
		 * enumeration-type.
		 */
		private Type(String tag, String format_type)
		{
			this.tag = tag;
			this.format_type = format_type;
		}

		@Override
		public String toString()
		{
			return this.tag;
		}

		/**
		 * With this method the enumeration-type can be found based on the given
		 * int conforming to java.sql.Types
		 * 
		 * @param type
		 *            The string-representation of the type.
		 * @return The enumeration-type.
		 */
		public static Type getType(int type)
		{
			switch (type)
			{
			// string
				case Types.CHAR:
					return STRING;
				case Types.VARCHAR:
					return STRING;
					// boolean
				case Types.BOOLEAN:
					return BOOL;
				case Types.BIT:
					return BOOL;
				case Types.TINYINT:
					return INT;
					// integer
				case Types.INTEGER:
					return INT;
				case Types.SMALLINT:
					return INT;
				case Types.BIGINT:
					return LONG;
					// decimal
				case Types.REAL:
					return DECIMAL;
				case Types.FLOAT:
					return DECIMAL;
				case Types.DOUBLE:
					return DECIMAL;
				case Types.DECIMAL:
					return DECIMAL;
					// text
				case Types.BLOB:
					return TEXT;
				case Types.CLOB:
					return TEXT;
				case Types.LONGVARCHAR:
					return TEXT;
					// date
				case Types.DATE:
					return DATE;
				case Types.TIME:
					return DATETIME;
				case Types.TIMESTAMP:
					return DATETIME;
				case Types.NUMERIC:
					return DECIMAL;
				case Types.LONGVARBINARY:
					return TEXT;
				default:
				{
					for (java.lang.reflect.Field f : java.sql.Types.class.getFields())
					{
						try
						{
							if (((Integer) f.get(null)).equals(type))
							{
								logger.error("Unknown type: " + f.getName());
								return UNKNOWN;
							}
						}
						catch (IllegalAccessException e)
						{
						}
					}

					logger.error("Unknown type: " + type);
					return UNKNOWN;
				}
			}
		}

		/**
		 * With this method the enumeration-type can be found based on the given
		 * string.
		 * 
		 * @param tag
		 *            The string-representation of the tag.
		 * @return The enumeration-type.
		 */
		public static Type getType(String tag)
		{
			if (tag.equals(BOOL.tag)) return BOOL;
			else if (tag.equals(INT.tag)) return INT;
			else if (tag.equals(LONG.tag)) return LONG;
			else if (tag.equals(DECIMAL.tag)) return DECIMAL;
			else if (tag.equals(STRING.tag)) return STRING;
			else if (tag.equals(TEXT.tag)) return TEXT;
			else if (tag.equals(DATE.tag)) return DATE;
			else if (tag.equals(DATETIME.tag)) return DATETIME;
			else if (tag.equals(USER.tag)) return USER;
			else if (tag.equals(FILE.tag)) return FILE;
			else if (tag.equals(ENUM.tag)) return ENUM;
			else if (tag.equals(XREF_SINGLE.tag)) return XREF_SINGLE;
			else if (tag.equals(XREF_MULTIPLE.tag)) return XREF_MULTIPLE;
			else if (tag.equals(HYPERLINK.tag)) return HYPERLINK;
			else if (tag.equals(LIST.tag)) return LIST;
			else
				return UNKNOWN;
		}

		/** The string-representation of the enumeration-type. */
		public final String tag;
		/** */
		public final String format_type;
	};

	// GETTERS and SETTERS
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public Type getType()
	{
		return type;
	}

	public void setType(Type type)
	{
		this.type = type;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getXrefField()
	{
		return xref_field;
	}

	public void setXrefField(String xrefField)
	{
		this.xref_field = xrefField;
	}

	public String getXrefEntity()
	{
		return xref_entity;
	}

	public void setXrefEntity(String xrefEntity)
	{
		this.xref_entity = xrefEntity;
	}

	public String getXrefLabel()
	{
		return xref_label;
	}

	public void setXrefLabel(String xref_label)
	{
		this.xref_label = xref_label;
	}

	public Boolean getAuto()
	{
		return this.auto;
	}

	public void setAuto(Boolean auto)
	{
		this.auto = auto;
	}

	public Boolean getNillable()
	{
		return this.nillable;
	}

	public void setNillable(Boolean nillable)
	{
		this.nillable = nillable;
	}

	public Boolean getUnique()
	{
		return unique;
	}

	public void setUnique(Boolean unique)
	{
		this.unique = unique;
	}

	public Boolean getReadonly()
	{
		return readonly;
	}

	public void setReadonly(Boolean readonly)
	{
		this.readonly = readonly;
	}

	public String getXRefName()
	{
		return xref_name;
	}

	public void setXRefName(String xref_name)
	{
		this.xref_name = xref_name;
	}

	public String getEnumoptions()
	{
		return enum_options;
	}

	public void setEnumoptions(String enumoptions)
	{
		this.enum_options = enumoptions;
	}

	public Integer getLength()
	{
		return length;
	}

	public void setLength(Integer length)
	{
		this.length = length;
	}

	public String getMrefName()
	{
		return mref_name;
	}

	public void setMrefName(String mref_name)
	{
		this.mref_name = mref_name;
	}

	public String getMrefLocalid()
	{
		return mref_localid;
	}

	public void setMrefLocalid(String mref_localid)
	{
		this.mref_localid = mref_localid;
	}

	public String getMrefRemoteid()
	{
		return mref_remoteid;
	}

	public void setMrefRemoteid(String mref_remoteid)
	{
		this.mref_remoteid = mref_remoteid;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}
}
