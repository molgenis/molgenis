package org.molgenis;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.fieldtypes.*;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static java.util.stream.Collectors.toList;

/**
 * Singleton class that holds all known field types in MOLGENIS. For each FieldType it can be defined how to behave in
 * mysql, java, etc. <br>
 *
 * @see FieldType interface
 */
public class MolgenisFieldTypes
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisFieldTypes.class);

	private static Map<String, FieldType> types = new TreeMap<String, FieldType>();
	private static boolean init = false;

	public enum AttributeType
	{
		BOOL, CATEGORICAL, CATEGORICAL_MREF, COMPOUND, DATE, DATE_TIME, DECIMAL, EMAIL, ENUM, FILE, HTML, HYPERLINK, INT, LONG, /**
	 * Field type that models the relationship between two entities A and B in which an element of A may be linked
	 * to many elements of B, but a member of B is linked to only one element of A.
	 */
	ONE_TO_MANY, MREF, SCRIPT, STRING, TEXT, XREF;

		private static final Map<String, AttributeType> strValMap;

		static
		{
			AttributeType[] dataTypes = AttributeType.values();
			strValMap = newHashMapWithExpectedSize(dataTypes.length);
			for (AttributeType dataType : dataTypes)
			{
				strValMap.put(getValueString(dataType), dataType);
			}
		}

		/**
		 * Returns the enum value for the given value string
		 *
		 * @param valueString value string
		 * @return enum value
		 */
		public static AttributeType toEnum(String valueString)
		{
			return strValMap.get(normalize(valueString));
		}

		/**
		 * Returns the value string for the given enum value
		 *
		 * @param value enum value
		 * @return value string
		 */
		public static String getValueString(AttributeType value)
		{
			return normalize(value.toString());
		}

		/**
		 * Returns the value strings for all enum types in the defined enum order
		 *
		 * @return value strings
		 */
		public static List<String> getOptionsLowercase()
		{
			return Arrays.stream(values()).map(AttributeType::getValueString).collect(toList());
		}

		private static String normalize(String valueString)
		{
			return StringUtils.remove(valueString, '_').toLowerCase();
		}
	}

	public static final FieldType BOOL = new BoolField();
	public static final FieldType CATEGORICAL = new CategoricalField();
	public static final FieldType CATEGORICAL_MREF = new CategoricalMrefField();
	public static final FieldType COMPOUND = new CompoundField();
	public static final FieldType DATE = new DateField();
	public static final FieldType DATETIME = new DatetimeField();
	public static final FieldType DECIMAL = new DecimalField();
	public static final FieldType EMAIL = new EmailField();
	public static final FieldType FILE = new FileField();
	public static final FieldType HTML = new HtmlField();
	public static final FieldType HYPERLINK = new HyperlinkField();
	public static final FieldType INT = new IntField();
	public static final FieldType LONG = new LongField();
	public static final FieldType MREF = new MrefField();
	public static final FieldType ONE_TO_MANY = new OneToManyField();
	public static final FieldType SCRIPT = new ScriptField();
	public static final FieldType STRING = new StringField();
	public static final FieldType TEXT = new TextField();
	public static final FieldType XREF = new XrefField();
	public static final FieldType ENUM = new EnumField();

	// FIXME Do not add public static final ENUM here, as it holds the enum options so it is different per attribute,
	// this should be fixed. The options should not be added to the field

	/**
	 * Initialize default field types
	 */
	private static void init()
	{
		if (!init)
		{
			addType(BOOL);
			addType(CATEGORICAL);
			addType(CATEGORICAL_MREF);
			addType(COMPOUND);
			addType(DATE);
			addType(DATETIME);
			addType(DECIMAL);
			addType(EMAIL);
			addType(new EnumField());
			addType(FILE);
			addType(HTML);
			addType(HYPERLINK);
			addType(INT);
			addType(LONG);
			addType(MREF);
			addType(ONE_TO_MANY);
			addType(STRING);
			addType(SCRIPT);
			addType(TEXT);
			addType(XREF);
			addType(ENUM);

			init = true;
		}

	}

	public static void addType(FieldType ft)
	{
		types.put(ft.getClass().getSimpleName().toLowerCase(), ft);
	}

	public static FieldType getType(String name)
	{
		init();

		FieldType fieldType = types.get(name.replaceAll("[_]", "") + "field");
		if (fieldType != null)
		{
			try
			{
				return fieldType.getClass().newInstance();
			}
			catch (InstantiationException e)
			{
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			LOG.warn("couldn't get type for name '" + name + "'");
			return null;
		}
	}

	public static FieldType get(Field f) throws MolgenisModelException
	{
		init();
		try
		{
			final FieldType ft = f.getType().getClass().newInstance();
			ft.setField(f);
			return ft;
		}
		catch (InstantiationException e)
		{
			LOG.error("", e);
			throw new MolgenisModelException(e.getMessage());
		}
		catch (IllegalAccessException e)
		{
			LOG.error("", e);
			throw new MolgenisModelException(e.getMessage());
		}
	}

	public static FieldType getTypeBySqlTypesCode(int sqlCode)
	{
		switch (sqlCode)
		{
			case java.sql.Types.BIGINT:
				return new LongField();

			case java.sql.Types.INTEGER:
			case java.sql.Types.SMALLINT:
			case java.sql.Types.TINYINT:
				return new IntField();

			case java.sql.Types.BOOLEAN:
			case java.sql.Types.BIT:
				return new BoolField();

			case java.sql.Types.DATE:
				return new DateField();

			case java.sql.Types.DECIMAL:
			case java.sql.Types.DOUBLE:
			case java.sql.Types.NUMERIC:
			case java.sql.Types.FLOAT:
			case java.sql.Types.REAL:
				return new DecimalField();

			case java.sql.Types.CHAR:
			case java.sql.Types.VARCHAR:
			case java.sql.Types.NVARCHAR:
			case java.sql.Types.BLOB:
			case java.sql.Types.CLOB:
			case java.sql.Types.LONGVARCHAR:
			case java.sql.Types.VARBINARY:
			case java.sql.Types.LONGNVARCHAR:
				return new StringField();

			case java.sql.Types.TIME:
			case java.sql.Types.TIMESTAMP:
				return new DatetimeField();

			default:
				LOG.error("UNKNOWN sql code: " + sqlCode);
				return null;
		}
	}
}
