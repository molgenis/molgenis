package org.molgenis;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.molgenis.fieldtypes.BoolField;
import org.molgenis.fieldtypes.DateField;
import org.molgenis.fieldtypes.DatetimeField;
import org.molgenis.fieldtypes.DecimalField;
import org.molgenis.fieldtypes.EmailField;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.FileField;
import org.molgenis.fieldtypes.FreemarkerField;
import org.molgenis.fieldtypes.HyperlinkField;
import org.molgenis.fieldtypes.ImageField;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.LongField;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.RichtextField;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.TextField;
import org.molgenis.fieldtypes.UnknownField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.HtmlInputException;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Field;

/**
 * Singleton class that holds all known field types in MOLGENIS. For each
 * FieldType it can be defined how to behave in mysql, java, hsqldb, etc. <br>
 * 
 * @see FieldType interface
 */
public class MolgenisFieldTypes
{
	private static Map<String, FieldType> types = new TreeMap<String, FieldType>();
	private static Logger logger = Logger.getLogger(MolgenisFieldTypes.class);
	private static boolean init = false;

	public enum FieldTypeEnum
	{
		BOOL, CHAR, DATE, DATE_TIME, DECIMAL, ENUM, EMAIL, FILE, FREEMARKER, HEXA, HYPERLINK, IMAGE, INT, LIST, LONG, MREF, STRING, TEXT, LONGTEXT, XREF, CATEGORICAL, UNKNOWN, RICHTEXT
	}

	/** Initialize default field types */
	private static void init()
	{
		if (!init)
		{
			addType(new BoolField());
			addType(new DateField());
			addType(new DatetimeField());
			addType(new DecimalField());
			addType(new EnumField());
			addType(new EmailField());
			addType(new FileField());
			addType(new HyperlinkField());
			addType(new ImageField());
			addType(new LongField());
			addType(new MrefField());
			addType(new StringField());
			addType(new TextField());
			addType(new XrefField());
			addType(new IntField());
			addType(new RichtextField());
			addType(new FreemarkerField());

			init = true;
		}

	}

	public static void addType(FieldType ft)
	{
		types.put(ft.getClass().getSimpleName().toLowerCase(), ft);
	}

	public static HtmlInput<?> createInput(String type, String name, String xrefEntityClassName)
			throws HtmlInputException
	{
		return getType(type).createInput(name, xrefEntityClassName);
	}

	public static FieldType getType(String name)
	{
		init();

		FieldType fieldType = types.get(name + "field");
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
			logger.warn("couldn't get type for name '" + name + "'");
			return new UnknownField();
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
			logger.error(e);
			throw new MolgenisModelException(e.getMessage());
		}
		catch (IllegalAccessException e)
		{
			logger.error(e);
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
				logger.error("UNKNOWN sql code: " + sqlCode);
				return new UnknownField();
		}
	}
}
