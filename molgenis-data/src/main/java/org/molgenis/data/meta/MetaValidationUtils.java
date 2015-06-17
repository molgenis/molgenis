package org.molgenis.data.meta;

import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.fieldtypes.CompoundField;

import com.google.common.collect.Sets;

public class MetaValidationUtils
{
	public static final int MAX_ATTRIBUTE_LENGTH = 30;

	// https://docs.oracle.com/javase/tutorial/java/nutsandbolts/_keywords.html
	// Case sensitive
	public static final Set<String> JAVA_KEYWORDS = Sets.newHashSet("abstract", "continue", "for", "new", "switch",
			"assert", "default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this", "break",
			"double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum",
			"instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final",
			"interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float",
			"native", "super", "while");

	// http://dev.mysql.com/doc/mysqld-version-reference/en/mysqld-version-reference-reservedwords-5-6.html
	// Version: 5.6
	// Case insensitive
	public static final Set<String> MYSQL_KEYWORDS = Sets.newHashSet("ACCESSIBLE", "ADD", "ALL", "ALTER", "ANALYZE",
			"AND", "AS", "ASC", "ASENSITIVE", "BEFORE", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL",
			"CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK", "COLLATE", "COLUMN", "CONDITION", "CONSTRAINT",
			"CONTINUE", "CONVERT", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
			"CURRENT_USER", "CURSOR", "DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE",
			"DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELAYED", "DELETE", "DESC", "DESCRIBE",
			"DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL", "EACH", "ELSE", "ELSEIF",
			"ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH", "FLOAT", "FLOAT4", "FLOAT8", "FOR",
			"FORCE", "FOREIGN", "FROM", "FULLTEXT", "GENERAL", "GET", "GRANT", "GROUP", "HAVING", "HIGH_PRIORITY",
			"HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IGNORE_SERVER_IDS", "IN", "INDEX",
			"INFILE", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INT1", "INT2", "INT3", "INT4", "INT8",
			"INTEGER", "INTERVAL", "INTO", "IO_AFTER_GTIDS", "IO_BEFORE_GTIDS", "IS", "ITERATE", "JOIN", "KEY", "KEYS",
			"KILL", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LINEAR", "LINES", "LOAD", "LOCALTIME",
			"LOCALTIMESTAMP", "LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "MASTER_BIND",
			"MASTER_HEARTBEAT_PERIOD", "MASTER_SSL_VERIFY_SERVER_CERT", "MATCH", "MAXVALUE", "MEDIUMBLOB", "MEDIUMINT",
			"MEDIUMTEXT", "MIDDLEINT", "MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT",
			"NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "ON", "ONE_SHOT", "OPTIMIZE", "OPTION", "OPTIONALLY", "OR",
			"ORDER", "OUT", "OUTER", "OUTFILE", "PARTITION", "PRECISION", "PRIMARY", "PROCEDURE", "PURGE", "RANGE",
			"READ", "READS", "READ_WRITE", "REAL", "REFERENCES", "REGEXP", "RELEASE", "RENAME", "REPEAT", "REPLACE",
			"REQUIRE", "RESIGNAL", "RESTRICT", "RETURN", "REVOKE", "RIGHT", "RLIKE", "SCHEMA", "SCHEMAS",
			"SECOND_MICROSECOND", "SELECT", "SENSITIVE", "SEPARATOR", "SET", "SHOW", "SIGNAL", "SLOW", "SMALLINT",
			"SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_AFTER_GTIDS",
			"SQL_BEFORE_GTIDS", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT", "SSL", "STARTING",
			"STRAIGHT_JOIN", "TABLE", "TERMINATED", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING",
			"TRIGGER", "TRUE", "UNDO", "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE", "USE", "USING",
			"UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING",
			"WHEN", "WHERE", "WHILE", "WITH", "WRITE", "XOR", "YEAR_MONTH", "ZEROFILL");

	// http://www.w3schools.com/js/js_reserved.asp
	// Case sensitive
	public static final Set<String> JAVASCRIPT_KEYWORDS = Sets.newHashSet("abstract", "arguments", "boolean", "break",
			"byte", "case", "catch", "char", "class", "const", "continue", "debugger", "default", "delete", "do",
			"double", "else", "enum", "eval", "export", "extends", "false", "final", "finally", "float", "for",
			"function", "goto", "if", "implements", "import", "in", "instanceof", "int", "interface", "let", "long",
			"native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super",
			"switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typeof", "var", "void",
			"volatile", "while", "with", "yield");

	// Case sensitive
	public static final Set<String> MOLGENIS_KEYWORDS = Sets.newHashSet("login", "logout", "csv", "entities",
			"attributes", "base");

	public static Set<String> KEYWORDS = Sets.newHashSet();
	static
	{
		KEYWORDS.addAll(JAVA_KEYWORDS);
		KEYWORDS.addAll(JAVASCRIPT_KEYWORDS);
		KEYWORDS.addAll(MOLGENIS_KEYWORDS);
		KEYWORDS.addAll(MYSQL_KEYWORDS);
	}

	/**
	 * Checks if a name is a reserved keyword.
	 */
	public static void checkForKeyword(String name)
	{
		if (KEYWORDS.contains(name) || KEYWORDS.contains(name.toUpperCase()))
		{
			throw new MolgenisDataException("Name [" + name + "] is not allowed because it is a reserved keyword.");
		}
	}

	/**
	 * Validates names of entities, packages and attributes. Rules: only [a-zA-Z0-9_#] are allowed, name must start with
	 * a letter
	 */
	public static void validateName(String name)
	{
		checkForKeyword(name);

		if (name.length() > MAX_ATTRIBUTE_LENGTH)
		{
			throw new MolgenisDataException("Attribute name [" + name + "] is too long: maximum length is "
					+ MAX_ATTRIBUTE_LENGTH + " characters.");
		}

		if (!name.matches("[a-zA-Z0-9_#]+"))
		{
			throw new MolgenisDataException("Invalid characters in: [" + name
					+ "] Only letters (a-z, A-Z), digits (0-9), underscores (_) and hashes (#) are allowed.");
		}

		if (Character.isDigit(name.charAt(0)))
		{
			throw new MolgenisDataException("Invalid name: [" + name + "] Names must start with a letter.");
		}
	}

	/**
	 * Recursively traverses attributes and validates the names.
	 */
	public static void validateAttributeNames(Iterable<AttributeMetaData> amds)
	{
		for (AttributeMetaData amd : amds)
		{
			validateName(amd.getName());
			if (amd.getDataType() instanceof CompoundField)
			{
				validateAttributeNames(amd.getAttributeParts());
			}
		}
	}

	/**
	 * Validates the names of an entity and all its attributes.
	 */
	public static void validateEntityMetaData(EntityMetaData emd)
	{
		validateName(emd.getSimpleName());

		try
		{
			validateAttributeNames(emd.getAttributes());
		}
		catch (MolgenisDataException e)
		{
			throw new MolgenisDataException("Validation error in entity [" + emd.getName() + "]: " + e.getMessage(), e);
		}
	}

}
