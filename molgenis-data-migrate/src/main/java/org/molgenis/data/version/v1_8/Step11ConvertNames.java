package org.molgenis.data.version.v1_8;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.AsyncJdbcTemplate;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.version.MolgenisUpgrade;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.common.collect.Sets;

/**
 * Part of migration to MOLGENIS 1.8.
 * 
 * Package/entity/attribute names must now bow to the following rules: 1. The only characters allowed are [a-zA-Z0-9_#].
 * 2. Names may not begin with digits. 3. The maximum length is 30 characters. 4. Keywords reserved by Java, JavaScript
 * and MySQL can not be used.
 * 
 * @author tommy
 */
public class Step11ConvertNames extends MolgenisUpgrade
{
	private JdbcTemplate template;
	private DataSource dataSource;
	private static final Logger LOG = LoggerFactory.getLogger(Step11ConvertNames.class);

	public Step11ConvertNames(DataSource dataSource)
	{
		super(10, 11);

		try
		{
			// InnoDB only allows setting foreign_key_checks=0 for a single session, so use a single connection source
			this.template = new JdbcTemplate(new SingleConnectionDataSource(dataSource.getConnection(), true));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		this.dataSource = dataSource;
	}

	@Override
	public void upgrade()
	{
		DataServiceImpl dataService = new DataServiceImpl();

		// Get the undecorated repos
		MysqlRepositoryCollection undecoratedMySQL = new MysqlRepositoryCollection()
		{
			@Override
			protected MysqlRepository createMysqlRepository()
			{
				return new MysqlRepository(dataService, dataSource, new AsyncJdbcTemplate(new JdbcTemplate(dataSource)));
			}

			@Override
			public boolean hasRepository(String name)
			{
				throw new NotImplementedException("Not implemented yet");
			}
		};
		MetaDataService metaData = new MetaDataServiceImpl(dataService);
		RunAsSystemProxy.runAsSystem(() -> metaData.setDefaultBackend(undecoratedMySQL));

		// MIGRATION STARTS HERE
		LOG.info("Validating JPA entities...");
		checkJPAentities();

		setForeignKeyConstraintCheck(false);

		LOG.info("Validating package names...");
		checkAndUpdatePackages(null, null);

		setForeignKeyConstraintCheck(true);

		try
		{
			// manually close the single datasource connection
			template.getDataSource().getConnection().close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		// TODO: remove
		throw new RuntimeException("EXIT");
	}

	/**
	 * Recursively checks the names of packages and changes the names when they are not valid.
	 */
	public void checkAndUpdatePackages(String parent, String parentFix)
	{
		List<Map<String, Object>> packages = template.queryForList(getPackagesWithParentSql(parent));
		if (packages.isEmpty()) return;

		// first add packages in this package to the scope
		HashSet<String> scope = Sets.newHashSet();
		packages.forEach(pack -> scope.add(pack.get("name").toString()));

		// iterate over the packages and check the names
		for (Map<String, Object> pack : packages)
		{
			String name = pack.get("name").toString();
			String nameFix = fixName(name);

			String fullName = pack.get("fullName").toString();

			String fullNameFix = fullName;

			if (parentFix != null) fullNameFix = String.format("%s_%s", parentFix, nameFix);

			if (!name.equals(nameFix))
			{
				// name wasn't valid, make sure the new one is unique for this scope
				if (scope.contains(nameFix))
				{
					nameFix = makeNameUnique(nameFix, scope);
				}

				LOG.info(String.format("In Package [%s]: package name [%s] is not valid. Changing to [%s]...", parent,
						name, nameFix));

				// update the scope with the new name
				scope.remove(name);
				scope.add(nameFix);

				// change the end of fullNameFix with the new package name
				fullNameFix = fullNameFix.replaceAll(name + "$", nameFix);

				// update fullname, name and parent in database
				template.execute(getUpdatePackageNamesSql(fullName, fullNameFix, nameFix, parentFix));

				checkAndUpdatePackages(fullName, fullNameFix);
			}
			else
			{
				// nothing chagned in this name, only update the parent
				template.execute(getUpdatePackageNamesSql(fullName, fullNameFix, nameFix, parentFix));
				checkAndUpdatePackages(fullName, fullNameFix);
			}
		}
	}

	public String getPackagesWithParentSql(String parentFullName)
	{
		String query = (parentFullName == null) ? "IS NULL" : String.format("= '%s'", parentFullName);

		return String.format("SELECT fullName, name, parent FROM packages WHERE parent %s;", query);
	}

	public String getUpdatePackageParentNameSql(String parent, String fullName)
	{
		parent = (parent == null) ? "NULL" : String.format("'%s'", parent);
		return String.format("UPDATE packages SET parent=%s WHERE fullName='%s';", parent, fullName);
	}

	public String getUpdatePackageNamesSql(String fullName, String fullNameFix, String nameFix, String parent)
	{
		parent = (parent == null) ? "NULL" : String.format("'%s'", parent);
		return String.format("UPDATE packages SET fullName='%s', name='%s', parent=%s WHERE fullName='%s';",
				fullNameFix, nameFix, parent, fullName);
	}

	/**
	 * Turns the MySQL foreign key check on or off.
	 */
	public void setForeignKeyConstraintCheck(boolean doCheck)
	{
		int check = (doCheck == true) ? 1 : 0;
		template.execute(String.format("SET FOREIGN_KEY_CHECKS = %d;", check));
	}

	/**
	 * Checks the JPA entities and totally freaks out when they don't validate. Migration is 'impossible' for these
	 * entities, so we stop the application and leave it to the user to change their models.
	 */
	public void checkJPAentities() throws RuntimeException
	{
		List<Map<String, Object>> jpaEntities = template
				.queryForList("SELECT fullName FROM entities WHERE backend = 'JPA'");

		for (Map<String, Object> jpaEntity : jpaEntities)
		{
			// check name of this JPA entity
			String entityName = jpaEntity.get("fullName").toString();

			if (!entityName.equals(fixName(entityName)))
			{
				throw new RuntimeException(
						"The JPA entity ["
								+ entityName
								+ "] did not pass validation. JPA entities cannot be automatically migrated. Please update the entities manually and rebuild the app.");
			}

			// check attributes of this JPA entity
			List<Map<String, Object>> jpaEntityAttributes = template
					.queryForList(fetchAttributesForEntitySql(entityName));

			for (Map<String, Object> attribute : jpaEntityAttributes)
			{
				String attributeName = attribute.get("name").toString();

				if (!attributeName.equals(fixName(attributeName)))
				{
					throw new RuntimeException(
							"The attribute ["
									+ attributeName
									+ "] of JPA entity ["
									+ entityName
									+ "] did not pass validation. JPA entities cannot be automatically migrated. Please update the entities manually and rebuild the app.");
				}
			}
		}
	}

	/**
	 * Changes invalid names using these rules: 1. Strip invalid characters and digits at the start of a name. 2. Append
	 * a '0' if a name is a reserved keyword. 3. Truncate long names so they are no longer than 30.
	 */
	public String fixName(String name)
	{
		// remove all invalid characters
		name = name.replaceAll("[^a-zA-Z0-9_#]+", "");

		// strip digits at start of string
		name = name.replaceAll("^[0-9]+", "");

		// append a digit after reserved keywords to make them usable again
		if (KEYWORDS.contains(name) || KEYWORDS.contains(name.toUpperCase()))
		{
			name += "0";
		}

		// truncate names longer than 30
		if (name.length() > MAX_NAME_LENGTH)
		{
			name = name.substring(0, MAX_NAME_LENGTH);
			// TODO: keyword check
		}

		return name;
	}

	/**
	 * Recursively adds a count at the end of a name until it is unique within its scope
	 */
	public String makeNameUnique(String name, int index, HashSet<String> scope)
	{
		int indexLength = String.valueOf(index).length();
		String newName;
		if ((name.length() + indexLength) <= MAX_NAME_LENGTH)
		{
			newName = name + index;
		}
		else
		{
			// adding numbers will exceed the max length limit, so cut some stuff off
			newName = name.substring(0, name.length() - indexLength);
			newName = newName + index;
		}

		if (scope.contains(newName))
		{
			return makeNameUnique(name, ++index, scope);
		}

		return newName;
	}

	public String makeNameUnique(String name, HashSet<String> scope)
	{
		return makeNameUnique(name, 1, scope);
	}

	private String fetchAttributesForEntitySql(String fullyQualifiedEntityName)
	{
		return "SELECT attributes.name FROM attributes INNER JOIN entities_attributes ON entities_attributes.attributes = attributes.identifier WHERE entities_attributes.fullName = '"
				+ fullyQualifiedEntityName + "'";
	};

	public static final int MAX_NAME_LENGTH = 30;

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
			"attributes");

	// TODO: REMOVE!!!!
	public static final Set<String> TEST = Sets.newHashSet("molgenis");

	public static Set<String> KEYWORDS = Sets.newHashSet();
	static
	{
		KEYWORDS.addAll(JAVA_KEYWORDS);
		KEYWORDS.addAll(JAVASCRIPT_KEYWORDS);
		KEYWORDS.addAll(MOLGENIS_KEYWORDS);
		KEYWORDS.addAll(MYSQL_KEYWORDS);
		KEYWORDS.addAll(TEST);
	}
}
