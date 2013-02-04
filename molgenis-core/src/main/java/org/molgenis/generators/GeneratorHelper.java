package org.molgenis.generators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisOptions;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.FileField;
import org.molgenis.fieldtypes.ImageField;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Field;
import org.molgenis.model.elements.Model;
import org.molgenis.model.elements.Unique;
import org.molgenis.util.tuple.Tuple;

public class GeneratorHelper
{
	private static final Logger logger = Logger.getLogger(GeneratorHelper.class.getSimpleName());
	MolgenisOptions options;
	MolgenisFieldTypes typeRegistry;

	public GeneratorHelper(MolgenisOptions options)
	{
		this.options = options;
		this.typeRegistry = new MolgenisFieldTypes();

	}

	/**
	 * Convert string with first character to uppercase.
	 * 
	 * @param string
	 * @return string with first character in uppercase.
	 */
	public static String firstToUpper(String string)
	{
		if (string == null) return " NULL ";
		if (string.length() > 0) return string.substring(0, 1).toUpperCase() + string.substring(1);
		else
			return " ERROR[STRING EMPTY] ";
	}

	/**
	 * Convert string with first character to lowercase.
	 * 
	 * @param string
	 * @return string with first character in lowercase.
	 */
	public static String firstToLower(String string)
	{
		if (string == null) return " NULL ";
		if (string.length() > 1) return string.substring(0, 1).toLowerCase() + string.substring(1);
		return string;
	}

	/**
	 * Convert string to full uppercase
	 * 
	 * @param string
	 * @return uppercase string
	 */
	public static String toUpper(String string)
	{
		if (string == null) return " NULL ";
		return string.toUpperCase();
	}

	/**
	 * Convert string to full lowercase
	 * 
	 * @param string
	 * @return lowercase string
	 */
	public static String toLower(String string)
	{
		if (string == null) return " NULL ";
		return string.toLowerCase();
	}

	/**
	 * Get the java type for a field.
	 * 
	 * @return the java type or UKNOWN
	 */
	public String getType(Field field) throws Exception
	{
		if (field == null) return "NULLPOINTER";
		try
		{
			return MolgenisFieldTypes.get(field).getJavaPropertyType();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "EXCEPTION";
		}
	}

	/**
	 * Get the cpp type for a field.
	 * 
	 * @return the java type or UKNOWN
	 */
	public String getCppType(Field field) throws Exception
	{
		if (field == null) return "void*";
		try
		{
			return MolgenisFieldTypes.get(field).getCppPropertyType();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "EXCEPTION";
		}
	}

	/**
	 * Get the cpp type for a field.
	 * 
	 * @return the java type or UKNOWN
	 */
	public String getCppJavaType(Field field) throws Exception
	{
		if (field == null) return "Ljava/lang/NULL;";
		try
		{
			return MolgenisFieldTypes.get(field).getCppJavaPropertyType();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "EXCEPTION";
		}
	}

	/**
	 * Java setter type of the field, e.g. getString() returns "String".
	 * 
	 * @param model
	 * @param field
	 * @return setter type
	 * @throws Exception
	 */
	public String getSetType(Model model, Field field) throws Exception
	{
		return MolgenisFieldTypes.get(field).getJavaSetterType();
	}

	/**
	 * Creates a default value based on the default values set in the model. If
	 * no defaultValue is provided and if the field is not "automatic" then the
	 * default value is set to "null" so the user has to decide.
	 * 
	 * @param model
	 *            Meta model
	 * @param field
	 *            Meta model of a field (question: couldn't we ask the field for
	 *            this??)
	 * @return the default value as String
	 * @throws Exception
	 */
	public String getDefault(Model model, Field field) throws Exception
	{
		return MolgenisFieldTypes.get(field).getJavaPropertyDefault();
	}

	public String getJavaAssignment(Field field, String value) throws MolgenisModelException
	{
		return MolgenisFieldTypes.get(field).getJavaAssignment(value);
	}

	/**
	 * Convert a list of string to comma separated values.
	 * 
	 * @param elements
	 * @return csv
	 */
	public String toCsv(List<String> elements)
	{
		StringBuilder strBuilder = new StringBuilder();

		if (elements != null)
		{
			for (String str : elements)
				strBuilder.append('\'').append(str).append('\'').append(',');
			if (!elements.isEmpty()) strBuilder.deleteCharAt(strBuilder.length() - 1);
		}

		return strBuilder.toString();
	}

	/**
	 * Get the mysql type of a field: VARCHAR, INT, etc.
	 * 
	 * @param model
	 * @param field
	 * @return string that represents the mysql value of a fieldtype.
	 * @throws Exception
	 */
	public String getMysqlType(Model model, Field field) throws Exception
	{
		return MolgenisFieldTypes.get(field).getMysqlType();
	}

	public String getOracleType(Model model, Field field) throws Exception
	{
		return MolgenisFieldTypes.get(field).getOracleType();
	}

	public String getXsdType(Model model, Field field) throws Exception
	{
		return MolgenisFieldTypes.get(field).getXsdType();
	}

	public String getHsqlType(Field field) throws Exception
	{
		Logger.getLogger("TEST").debug("trying " + field);
		return MolgenisFieldTypes.get(field).getHsqlType();
	}

	public Vector<Field> getAddFields(Entity e) throws Exception
	{
		return this.getAddFields(e, false);
	}

	/**
	 * Get the fields that participate in an insert (so excluding automatic
	 * fields).
	 * 
	 * @param e
	 * @param includeKey
	 * @return vector of fields that are not automatic values
	 * @throws Exception
	 */
	public Vector<Field> getAddFields(Entity e, boolean includeKey) throws Exception
	{
		Vector<Field> add_fields = new Vector<Field>();

		if (options.object_relational_mapping.equals(MolgenisOptions.CLASS_PER_TABLE))
		{
			for (Field f : getAllFields(e))
			{
				// get rid of mref,
				// get rid of automatic id
				// get rid of "type" enum field when not root ancestor
				if (!isMref(f) && (!isAutoId(f, e) || includeKey))
				// TODO: fix automatic fields
				// MAJOR error, arghhhh!!! &&
				// !getKeyFields(PRIMARY_KEY).contains(f))
				{
					add_fields.add(f);
				}
			}
		}
		else if (options.object_relational_mapping.equals(MolgenisOptions.SUBCLASS_PER_TABLE))
		{
			for (Field f : e.getImplementedFields())
			{
				// get rid of mref,
				// get rid of automatic id
				// get rid of "type" enum field when not root ancestor
				boolean inheritedField = (f.getEntity().getAncestor() != null && f.getEntity().getAncestor()
						.getAllFields().contains(f));
				if (!isMref(f) && (!isAutoId(f, e) || includeKey || inheritedField))
				// TODO: fix automatic fields
				// MAJOR error, arghhhh!!! &&
				// !getKeyFields(PRIMARY_KEY).contains(f))
				{

					add_fields.add(f);
				}
			}
			// if(e.hasAncestor()) {
			// add_fields.add(e.getPrimaryKey());
			// }
		}

		return add_fields;
	}

	/**
	 * Test wether the field is an mref.
	 * 
	 * @param f
	 * @return
	 */
	private boolean isMref(Field f)
	{
		return f.getType() instanceof MrefField;
	}

	/**
	 * Test wether the field as a "type" field.
	 * 
	 * @param f
	 * @param e
	 * @return
	 */
	private boolean isTypeField(Field f, Entity e)
	{
		return !e.isRootAncestor() && f.getType() instanceof EnumField && f.getName() == Field.TYPE_FIELD;
	}

	private boolean isAutoId(Field f, Entity e)
	{
		return f.getType() instanceof IntField && f.isAuto();
		// SOLVED BY TRIGGERS && f.getEntity() == e;
	}

	public Vector<Field> getAllFields(Entity e) throws Exception
	{
		return getAllFields(e, "");
	}

	public Vector<Field> getAllFields(Entity e, String type) throws Exception
	{
		Vector<Field> all_fields = e.getAllFields();

		for (Field f : e.getAllFields())
		{
			if (!all_fields.contains(f) && (type.equals("") || f.getType().toString().equals(type)))
			{
				all_fields.add(f);
			}
		}

		return all_fields;
	}

	/**
	 * The table fields of this entity
	 */
	public Vector<Field> getDbFields(Entity e, String type) throws Exception
	{
		Vector<Field> db_fields = new Vector<Field>();
		if (options.object_relational_mapping.equals(MolgenisOptions.CLASS_PER_TABLE))
		{
			Vector<Field> all_fields = getAllFields(e, type);

			for (Field f : all_fields)
			{
				if (!(f.getType() instanceof MrefField) // && (f.getName() !=
														// "type" ||
														// e.isRootAncestor())
						&& (type.equals("") || f.getType().toString().equals(type)))
				{
					db_fields.add(f);
				}
			}
		}
		else if (options.object_relational_mapping.equals(MolgenisOptions.SUBCLASS_PER_TABLE))
		{
			Vector<Field> local_fields = e.getImplementedFields();

			for (Field f : local_fields)
			{
				if (!(f.getType() instanceof MrefField) // && (f.getName() !=
														// "type" ||
														// e.isRootAncestor())
						&& (type.equals("") || f.getType().toString().equals(type)))
				{
					db_fields.add(f);
				}
			}

			// if(e.hasAncestor()) {
			// db_fields.add(e.getPrimaryKey());
			// }
		}
		// String field_names = "";
		// for(Field f: db_fields) field_names += f.getName()+" ";
		// logger.error("dbFields for "+e.getName()+": "+field_names);
		return db_fields;
	}

	/**
	 * The queryable fields of the entity (in case of inheritance from the view
	 * join)
	 * 
	 * @param e
	 * @param type
	 * @throws Exception
	 */
	public Vector<Field> getViewFields(Entity e, String type) throws Exception
	{
		Vector<Field> view_fields = new Vector<Field>();

		Vector<Field> all_fields = getAllFields(e, type);

		for (Field f : all_fields)
		{
			if (!(f.getType() instanceof MrefField) && (type.equals("") || f.getType().toString().equals(type)))
			{
				view_fields.add(f);
			}
		}

		return view_fields;
	}

	public Vector<Field> getUpdateFields(Entity e) throws Exception
	{
		Vector<Field> all_update_fields = new Vector<Field>();

		List<Field> fields = null;
		if (e.getImplementedFields().size() > e.getFields().size())
		{
			fields = e.getImplementedFields();
		}
		else
		{
			fields = e.getFields();
		}

		for (Field f : fields)
		{
			// exclude readonly, unless it is the id or a file filed
			if (!isMref(f)
					&& !isTypeField(f, e)
					&& (!f.isReadOnly() || isPrimaryKey(f, e) || f.getType() instanceof FileField || f.getType() instanceof ImageField))
			{
				all_update_fields.add(f);
			}
		}

		return all_update_fields;
	}

	public boolean isPrimaryKey(Field f, Entity e) throws MolgenisModelException
	{
		return e.getKeyFields(0).contains(f);
	}

	public Vector<Field> getKeyFields(Entity e) throws MolgenisModelException
	{
		return e.getKeyFields(0);
	}

	public Vector<Unique> getAllKeys(Entity e) throws MolgenisModelException
	{
		Vector<Unique> all_keys = new Vector<Unique>();

		if (e.getAncestor() != null)
		{
			all_keys.addAll(getAllKeys(e.getAncestor()));
		}
		for (Unique u : e.getKeys())
		{
			if (!all_keys.contains(u))
			{
				all_keys.add(u);
			}
		}

		return all_keys;
	}

	/**
	 * Return all secondary keys for an entity
	 * 
	 * @param e
	 *            entity
	 * @return list of unique
	 * @throws MolgenisModelException
	 */
	public Vector<Unique> getSecondaryKeys(Entity e) throws MolgenisModelException
	{
		Vector<Unique> allkeys = getAllKeys(e);
		Vector<Unique> skeys = new Vector<Unique>();
		if (allkeys.size() > 1) for (int i = 1; i < allkeys.size(); i++)
		{
			skeys.add(allkeys.get(i));
		}
		return skeys;
	}

	/**
	 * Return all secondary key fields. If two secondary keys share a field, its
	 * only returned once.
	 * 
	 * @param keys
	 *            list of Unique definitions
	 * @return vector of fields that are part of a unique constraint
	 * @throws MolgenisModelException
	 */
	public Vector<Field> getKeyFields(List<Unique> keys) throws MolgenisModelException
	{
		Map<String, Field> result = new LinkedHashMap<String, Field>();
		for (Unique u : keys)
		{
			for (Field f : u.getFields())
			{
				if (result.get(f.getName()) == null)
				{
					result.put(f.getName(), f);
				}
			}
		}
		return new Vector<Field>(result.values());
	}

	public Vector<Field> getSecondaryKeyFields(Entity e) throws MolgenisModelException
	{
		List<Unique> keys = this.getSecondaryKeys(e);
		Map<String, Field> result = new LinkedHashMap<String, Field>();
		for (Unique u : keys)
		{
			for (Field f : u.getFields())
			{
				if (result.get(f.getName()) == null)
				{
					result.put(f.getName(), f);
				}
			}
			break;
		}
		return new Vector<Field>(result.values());
	}

	public Vector<Field> getKeyFields(Unique u) throws MolgenisModelException
	{
		return u.getFields();
	}

	/**
	 * A table can only contain the keys for columns that are actually in the
	 * table. In subclass_per_table mapping this requirement is not satisfied.
	 * These keys are ommited, and a warning is shown that these keys are not
	 * enforced.
	 * 
	 * @param e
	 * @return Vector of Unique (singular or complex keys)
	 * @throws MolgenisModelException
	 */
	public Vector<Unique> getTableKeys(Entity e) throws MolgenisModelException
	{
		Vector<Unique> all_keys = getAllKeys(e);
		Vector<Unique> table_keys = new Vector<Unique>();

		if (options.object_relational_mapping.equals(MolgenisOptions.SUBCLASS_PER_TABLE))
		{

			for (Unique aKey : all_keys)
			{
				boolean inTable = true;
				String field = null;
				for (Field f : aKey.getFields())
				{
					if (!e.getFields().contains(f))
					{
						inTable = false;
						field = f.getName();
					}
				}
				if (inTable) table_keys.add(aKey);
				else
					logger.warn("key " + aKey + " cannot be enforced on entity " + e.getName() + ": column '" + field
							+ "' is not in the subclass table.");
			}
		}

		return table_keys;
	}

	public Field getXrefField(Model model, Field e) throws Exception
	{
		return e.getXrefEntity().getField(e.getXrefFieldName());
	}

	public FieldType getFieldType(Model model, Field field) throws Exception
	{
		FieldType type = field.getType();
		if (type instanceof XrefField || type instanceof MrefField)
		{
			// Entity e_ref = field.getXrefEntity();
			Field f_ref = field.getXrefField();
			return getFieldType(model, f_ref);
		}
		else
		{
			return type;
		}

	}

	/**
	 * First character to upercase. And also when following "_";
	 * 
	 * @param name
	 * @return
	 */
	public static String getJavaName(String name)
	{
		return getJavaName(name, true);
	}

	public static String getJavaName(String name, boolean doFirstToUpper)
	{
		if (name == null) return " NULL ";

		String[] split = name.split("_");
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < split.length; i++)
		{
			if (i > 0) strBuilder.append('_');
			if (!split[i].isEmpty()) strBuilder.append(doFirstToUpper ? firstToUpper(split[i]) : split[i]);
		}

		return strBuilder.toString();
	}

	public List<Entity> getSubclasses(Entity superclass, Model m)
	{
		List<Entity> result = new ArrayList<Entity>();
		result.add(superclass);

		String name = superclass.getName();
		for (Entity e : m.getEntities())
		{
			if (e.getParents().contains(name))
			{
				result.addAll(getSubclasses(e, m));
			}
		}
		// logger.debug("found "+result.size()+ " subclases");
		return result;
	}

	public List<Entity> getSuperclasses(Entity subclass, Model m)
	{
		List<Entity> result = new ArrayList<Entity>(subclass.getAllAncestors());
		result.add(subclass);
		// Collections.reverse(result);
		return result;
	}

	public String pluralOf(String string)
	{
		return string + "s";
		// return Noun.pluralOf(string,Locale.ENGLISH);
	}

	/**
	 * Thank you, AndroMDA project... Linguistically pluralizes a singular noun.
	 * <p/>
	 * <ul>
	 * <li><code>noun</code> becomes <code>nouns</code></li>
	 * <li><code>key</code> becomes <code>keys</code></li>
	 * <li><code>word</code> becomes <code>words</code></li>
	 * <li><code>property</code> becomes <code>properties</code></li>
	 * <li><code>bus</code> becomes <code>busses</code></li>
	 * <li><code>boss</code> becomes <code>bosses</code></li>
	 * </ul>
	 * <p>
	 * Whitespace as well as <code>null></code> arguments will return an empty
	 * String.
	 * </p>
	 * 
	 * @param singularNoun
	 *            A singularNoun to pluralize
	 * @return The plural of the argument singularNoun
	 */
	public static String pluralize(String singularNoun)
	{
		throw new UnsupportedOperationException();
		// return Pluralizer.getInstance().pluralize(singularNoun);

		/*
		 * String pluralNoun = singularNoun;
		 * 
		 * int nounLength = pluralNoun.length();
		 * 
		 * if (nounLength == 1) { pluralNoun = pluralNoun + 's'; } else if
		 * (nounLength > 1) { char secondToLastChar =
		 * pluralNoun.charAt(nounLength - 2);
		 * 
		 * if (pluralNoun.endsWith("y")) { switch (secondToLastChar) { case 'a'
		 * : // fall-through case 'e' : // fall-through case 'i' : //
		 * fall-through case 'o' : // fall-through case 'u' : pluralNoun =
		 * pluralNoun + 's'; break; default : pluralNoun =
		 * pluralNoun.substring(0, nounLength - 1) + "ies"; } } else if
		 * (pluralNoun.endsWith("s")) { switch (secondToLastChar) { case 's' :
		 * pluralNoun = pluralNoun + "es"; break; default : pluralNoun =
		 * pluralNoun + "ses"; } } else { pluralNoun = pluralNoun + 's'; } }
		 * return pluralNoun;
		 */
	}

	public String parseQueryOperator(String label)
	{
		if (label.equals("EQUALS")) return "EQUALS";
		else if (label.equals("IN")) return "IN";
		else if (label.equals("LESS")) return "LESS";
		else if (label.equals("LESS_EQUAL")) return "LESS_EQUAL";
		else if (label.equals("GREATER")) return "GREATER";
		else if (label.equals("GREATER_EQUAL")) return "GREATER_EQUAL";
		else if (label.equals("LIKE")) return "LIKE";
		else if (label.equals("NOT")) return "NOT";
		else if (label.equals("LIMIT")) return "LIMIT";
		else if (label.equals("OFFSET")) return "OFFSET";
		else if (label.equals("SORTASC")) return "SORTASC";
		else if (label.equals("SORTDESC")) return "SORTDESC";
		else if (label.equals("NESTED")) return "NESTED";
		else if (label.equals("LAST")) return "LAST";

		return "UNKNOWN";
	}

	public static String escapeXml(String nonXml)
	{
		return StringEscapeUtils.escapeXml(nonXml);
	}

	public String getImports(Model m, Entity e, String subpackage, String suffix) throws MolgenisModelException
	{
		String sfx = suffix;
		String subPkg = subpackage;
		if (sfx == null) sfx = "";
		if (subPkg != null)
		{
			subPkg = subPkg.trim();
			if (!subPkg.equals(""))
			{
				if (!subPkg.startsWith(".")) subPkg = "." + subPkg;
				if (!subPkg.endsWith(".")) subPkg = subPkg + ".";
			}
			else
			{
				subPkg = ".";
			}

		}
		else
		{
			subPkg = ".";
		}

		// import referenced fields
		List<String> imports = new ArrayList<String>();
		for (Field f : e.getAllFields())
		{
			if (f.getType() instanceof XrefField || f.getType() instanceof MrefField)
			{

				String fullClassName = f.getXrefEntity().getNamespace() + subPkg + getJavaName(f.getXrefEntityName())
						+ sfx;
				if (!imports.contains(fullClassName))
				{
					imports.add(fullClassName);
				}
			}

			// import link tables
			// if (f.getType().equals(Field.Type.XREF_MULTIPLE))
			// {
			//
			// Entity linktable = m.getEntity(f.getMrefName());
			// if(linktable != null)
			// {
			// String fullClassName = linktable.getNamespace() + subPkg
			// + this.firstToUpper(linktable.getName())+sfx;
			// if (!imports.contains(fullClassName))
			// {
			// imports.add(fullClassName);
			// }
			// }
			// }
		}

		// import self
		String fullClassName = e.getNamespace() + subPkg + getJavaName(e.getName()) + sfx;
		if (!imports.contains(fullClassName))
		{
			imports.add(fullClassName);
		}

		// import parents
		// for(String superclass: e.getParents())
		// {
		// Entity parentEntity = m.getEntity(superclass);
		// fullClassName = parentEntity.getNamespace() + subPkg +
		// this.firstToUpper(parentEntity.getName())+sfx;
		// if (!imports.contains(fullClassName))
		// {
		// imports.add(fullClassName);
		// }
		// }

		StringBuilder strBuilder = new StringBuilder();
		for (String i : imports)
		{
			strBuilder.append("import ").append(i).append(";\n");
		}
		return strBuilder.toString();
	}

	public List<Tuple> loadExampleData(String fileName) throws IOException
	{
		final List<Tuple> result = new ArrayList<Tuple>();

		File dir = new File(this.options.example_data_dir);
		if (dir.exists())
		{
			File file = new File(dir.getAbsoluteFile() + "/" + fileName);
			if (file.exists())
			{
				CsvReader csvReader = new CsvReader(file);
				try
				{
					for (Tuple tuple : csvReader)
					{
						result.add(tuple);
					}

				}
				finally
				{
					csvReader.close();
				}
			}
		}

		return result;
	}

	public String renderExampleData(String fileName) throws IOException
	{
		List<Tuple> source = this.loadExampleData(fileName);
		StringBuilder strBuilder = new StringBuilder();

		if (!source.isEmpty())
		{
			Iterable<String> fields = source.get(0).getColNames();
			for (String field : fields)
			{
				strBuilder.append(field).append('\t');
			}
			strBuilder.append('\n');

			for (Tuple t : source)
			{
				for (String field : fields)
				{
					strBuilder.append(t.getString(field)).append('\t');
				}
				strBuilder.append('\n');
			}
		}

		return strBuilder.toString();
	}

	public String getTypeFieldName()
	{
		return Field.TYPE_FIELD;
	}
}
