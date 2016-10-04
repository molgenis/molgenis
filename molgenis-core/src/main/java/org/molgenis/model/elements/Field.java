package org.molgenis.model.elements;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.fieldtypes.*;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.util.SimpleTree;
import org.molgenis.util.Tree;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * Describes a field in an entity.
 *
 * @author RA Scheltema
 * @author MA Swertz
 * @version 1.0.0
 */
public class Field implements Serializable
{
	public static final String TYPE_FIELD = "__Type";
	/**
	 * Fixed value used for determining the not-set value for the varchar.
	 */
	private static final int LENGTH_NOT_SET = 0;

	public Field(Entity parent, String name, FieldType type)
	{
		this(parent, type, name, name, false, false, false, null, null);
	}

	// constructor(s)

	/**
	 * Standard constructor, which sets all the common variables for a field. Extra fields can be set with the
	 * appropriate access methods.
	 *
	 * @param type       The type of the field.
	 * @param name       The name of the field, which needs to be unique for the entity.
	 * @param label      The label of the field, which is used for the user interface.
	 * @param auto       Indicates whether this field needs to assigned a value by the database.
	 * @param nillable   Indicates whether this field can have the value NULL in the database.
	 * @param readonly   Indicates whether this field is readonly.
	 * @param jpaCascade Makes it possible to use JPA Cascade options to streamline object datbase interaction, see JPA
	 *                   documentation for details
	 */
	public Field(Entity parent, FieldType type, String name, String label, boolean auto, boolean nillable,
			boolean readonly, String default_value, String jpaCascade)
	{
		this.entity = parent;

		// global
		this.type = type;

		this.name = name;
		this.label = label;
		this.auto = auto;
		this.nillable = nillable;
		this.readonly = readonly;
		this.default_value = default_value;
		this.description = "";
		this.default_code = "";

		// varchar
		this.varchar_length = LENGTH_NOT_SET;

		// xref
		this.xref_entity = "";
		this.xref_field = "";
		this.xref_labels = new ArrayList<String>();

		//
		this.system = false;
		this.user_data = null;

		this.jpaCascade = jpaCascade;
	}

	public Field(String name)
	{
		this.name = name;
		this.type = new StringField();
	}

	// constructor(s)

	/**
	 * Standard constructor, which sets all the common variables for a field. Extra fields can be set with the
	 * appropriate access methods.
	 *
	 * @param type     The type of the field.
	 * @param name     The name of the field, which needs to be unique for the entity.
	 * @param label    The label of the field, which is used for the user interface.
	 * @param auto     Indicates whether this field needs to assigned a value by the database.
	 * @param nillable Indicates whether this field can have the value NULL in the database.
	 * @param readonly Indicates whether this field is readonly.
	 */
	public Field(Entity parent, FieldType type, String name, String label, boolean auto, boolean nillable,
			boolean readonly, String default_value)
	{
		this(parent, type, name, label, auto, nillable, readonly, default_value, null);
	}

	/**
	 * copy-constructor
	 */
	public Field(Field field)
	{
		this.auto = field.auto;
		this.default_code = field.default_code;
		this.default_value = field.default_value;
		this.description = field.description;
		this.entity = field.entity;
		this.enum_options = field.enum_options;
		this.filter = field.filter;
		this.filterfield = field.filterfield;
		this.filtertype = field.filtertype;
		this.filtervalue = field.filtervalue;
		this.hidden = field.hidden;
		this.label = field.label;
		this.mref_name = field.mref_name;
		this.mref_localid = field.mref_localid;
		this.mref_remoteid = field.mref_remoteid;
		this.name = field.name;
		this.nillable = field.nillable;
		this.readonly = field.readonly;
		this.system = field.system;
		this.type = field.type;
		this.user_data = field.user_data;
		this.varchar_length = field.varchar_length;
		this.xref_field = field.xref_field;
		this.xref_labels = field.xref_labels;
		this.xref_entity = field.xref_entity;
	}

	// global access methods

	/**
	 *
	 */
	@Deprecated
	public Entity getParent()
	{
		return entity;
	}

	public Entity getEntity()
	{
		return entity;
	}

	/**
	 * This method returns the type of this field.
	 *
	 * @return The type of this field.
	 */
	public FieldType getType()
	{
		return this.type;
	}

	/**
	 * @param type
	 */
	public void setType(FieldType type)
	{
		this.type = type;
	}

	public String getFormatString()
	{
		if (type instanceof XrefField || type instanceof MrefField)
		{
			try
			{
				return this.getXrefField().getFormatString();
			}
			catch (Exception e)
			{
			}
		}

		return type.getFormatString();
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
	 *
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * This method returns the label of this field.
	 *
	 * @return The label of this field.
	 */
	public String getLabel()
	{
		if (label == null)
		{
			return getName();
		}
		return this.label;
	}

	/**
	 *
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	public void setAuto(boolean auto)
	{
		this.auto = auto;
	}

	/**
	 * Returns whether this field is auto-assigned by the database.
	 *
	 * @return True when this field is auto-assigned, false otherwise.
	 */
	public boolean isAuto()
	{
		return this.auto;
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

	public boolean isAggregateable()
	{
		return aggregateable;
	}

	public void setAggregateable(boolean aggregateable)
	{
		this.aggregateable = aggregateable;
	}

	public void setReadonly(boolean readonly)
	{
		this.readonly = readonly;
	}

	public boolean isHidden()
	{
		return hidden;
	}

	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}

	/**
	 * Returns whether this field is read-only in the database.
	 *
	 * @return True when this field is read-only, false otherwise.
	 */
	public boolean isReadOnly()
	{
		return this.readonly;
	}

	/**
	 * Returns whether this field is a system-field. When it is a system-field, it will not be displayed in the
	 * user-interface.
	 *
	 * @return True when this field is a system-field, false otherwise.
	 */
	public boolean isSystem()
	{
		return this.system;
	}

	/**
	 * With this set-function the system-property can be set.
	 *
	 * @param s The system boolean.
	 */
	public void setSystem(boolean s)
	{
		this.system = s;
	}

	/**
	 * Returns whether this field is locally available in the table, or whether it is located in another table (for
	 * example link-table).
	 *
	 * @return Whether this field is located in the table
	 */
	public boolean isLocal()
	{
		return this.type instanceof MrefField;
	}

	/**
	 * @throws MolgenisModelException
	 */
	public boolean isCyclic() throws MolgenisModelException
	{
		if (!(this.type instanceof XrefField))
		{
			return false;
		}

		if (xref_entity.equals(this.name))
		{
			return true;
		}

		DBSchema root = entity.getRoot();
		Entity e = (Entity) root.get(xref_entity);
		for (Field field : e.getAllFields())
		{
			if (!(field.type instanceof XrefField))
			{
				continue;
			}

			if (field.xref_entity.equals(this.name))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns whether this field has enum options.
	 *
	 * @return Whether this field has enum options.
	 */
	public boolean isEnum()
	{
		return type instanceof EnumField;
	}

	/**
	 * Returns whether this field is a xref.
	 *
	 * @return Whether this field is a xref.
	 */
	// FIXME rename to isXref
	public boolean isXRef()
	{
		return type instanceof XrefField || type instanceof MrefField;
	}

	/**
	 * Returns whether this field is a mref.
	 *
	 * @return Whether this field is a mref.
	 */
	// FIXME rename to isMref
	public boolean isMRef()
	{
		return type instanceof MrefField;
	}

	/**
	 * Returns the value the database should set for the field when there is no value set.
	 *
	 * @return The default-value.
	 */
	public String getDefaultValue()
	{
		return this.default_value;
	}

	public void setDefaultValue(String value)
	{
		this.default_value = value;
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
	public void setEnumOptions(Vector<String> options)
	{
		this.enum_options = options;
	}

	public Vector<String> getEnumOptions() throws MolgenisModelException
	{
		if (!(this.type instanceof EnumField))
		{
			throw new MolgenisModelException("Field is not a ENUM, so options cannot be set.");
		}

		return this.enum_options;
	}

	// varchar access methods

	/**
	 * When this field is of type Type.VARCHAR, this method sets the maximum length the varchar can be. When this field
	 * is not of type Type.VARCHAR, this method raises an exception.
	 *
	 * @param length The maximum length the varchar field can be.
	 * @throws Exception When the field is not of type Type.VARCHAR.
	 */
	public void setVarCharLength(int length) // throws Exception
	{
		this.varchar_length = length;
	}

	/**
	 * When this field is of type Type.VARCHAR, this method returns the maximum length the varchar can be. When this
	 * field is not of type Type.VARCHAR, this method raises an exception.
	 *
	 * @return The maximum length the varchar field can be.
	 * @throws Exception When the field is not of type Type.VARCHAR.
	 */
	public int getVarCharLength() throws MolgenisModelException
	{
		if (!(this.type instanceof StringField))
		{
			throw new MolgenisModelException("Field is not a VARCHAR, so length cannot be retrieved.");
		}

		return this.varchar_length;
	}

	// xref access methods
	// FIXME rename setXRefEntity to setXrefEntityName (R --> r, +Name)
	public void setXRefEntity(String xref_entity)
	{
		this.xref_entity = xref_entity;
	}

	/**
	 * With this method all the additional information for this xref-field can be set. When this field is not of type
	 * Type.XREF_SINGLE or Type.XREF_MULTIPLE an exception is raised.
	 *
	 * @param entity The entity this field references.
	 * @param field  The field of the entity this field references.
	 * @param labels The label of this xref.
	 * @throws Exception When this field is not of type Type.XREF_SINGLE or Type.XREF_MULTIPLE
	 */
	// FIXME rename setXRefVariables to setXrefVariables
	public void setXRefVariables(String entity, String field, List<String> labels)
	{
		this.xref_entity = entity;
		this.xref_field = field;
		this.xref_labels = labels;
	}

	/**
	 * Returns the name of the entity this field is referencing to. When this field is not of type Type.XREF_SINGLE or
	 * Type.XREF_MULTIPLE an exception is raised.
	 *
	 * @return The name of the entity this field is referencing.
	 * @throws Exception When this field is not of type Type.XREF_SINGLE or Type.XREF_MULTIPLE
	 */
	public Entity getXrefEntity() throws MolgenisModelException
	{
		Entity e = this.getEntity().getModel().getEntity(this.getXrefEntityName());
		if (e == null)
		{
			throw new MolgenisModelException(
					"Xref entity '" + this.getXrefEntityName() + "' for attribute '" + this.getName() + "' of entity '"
							+ this.getEntity().getName() + "' not part of model.");
		}
		return e;
	}

	public String getXrefEntityName() throws MolgenisModelException
	{
		if (!(this.type instanceof XrefField) && !(this.type instanceof MrefField))
		{
			throw new MolgenisModelException("Field '" + this.getEntity().getName() + "." + this.getName()
					+ "' is not a XREF, so xref-table cannot be retrieved.");
		}

		return this.xref_entity;

	}

	/**
	 * Returns the name of the field of the entity this field is referencing to. When this field is not of type
	 * Type.XREF_SINGLE or Type.XREF_MULTIPLE an exception is raised.
	 *
	 * @return The name of the field of the entity this field is referencing.
	 * @throws Exception
	 * @throws Exception When this field is not of type Type.XREF_SINGLE or Type.XREF_MULTIPLE
	 */
	public Field getXrefField() throws MolgenisModelException
	{
		if (!(this.type instanceof XrefField) && !(this.type instanceof MrefField))
		{
			throw new MolgenisModelException("Field is not a XREF, so xref-field cannot be retrieved.");
		}

		Field result = this.getXrefEntity().getAllField(this.getXrefFieldName());
		if (result == null)
		{
			throw new MolgenisModelException(
					"xref_field is not known for field " + getEntity().getName() + "." + getName());
		}
		return result;
	}

	// FIXME consistency: throw MolgenisModelException if type is not correct
	public String getXrefFieldName()
	{
		return this.xref_field;
	}

	/**
	 * Returns the label of this reference. When this field is not of type Type.XREF_SINGLE or Type.XREF_MULTIPLE an
	 * exception is raised.
	 *
	 * @return The label of this reference.
	 * @throws MolgenisModelException
	 * @throws Exception              When this field is not of type Type.XREF_SINGLE or Type.XREF_MULTIPLE
	 */
	public List<String> getXrefLabelNames() throws MolgenisModelException
	{
		// label name = replace '.' and replace entity name if label entity ==
		// xref_entity
		List<String> label_names = new ArrayList<String>();
		for (String label : this.getXrefLabelsTemp())
		{
			label_names.add(label.replace(this.getXrefEntityName() + ".", ""));
		}
		return label_names;
	}

	/**
	 * Return a tree wich describes the path to xref labels. This allows also to use indirect secondary keys as labels.
	 * For example: Sample is identified by {name,Investigation.name}. Investigation.name has a path via
	 * sample.investigation.
	 *
	 * @return
	 * @throws MolgenisModelException
	 */
	public SimpleTree<SimpleTree<?>> getXrefLabelTree() throws MolgenisModelException
	{
		return getXrefLabelTree(true);
	}

	public SimpleTree<SimpleTree<?>> getXrefLabelTree(boolean useJavaNames) throws MolgenisModelException
	{
		List<String> labels = new ArrayList<String>();
		for (String label : this.getXrefLabelNames())
		{
			labels.add(getName() + "_" + getJavaName(label, useJavaNames));
		}

		SimpleTree<SimpleTree<?>> root = new SimpleTree<SimpleTree<?>>(getName(), null);
		root.setValue(this);
		this.getXrefLabelTree(labels, root, useJavaNames);
		return root;
	}

	/**
	 * Creates a tree with leafs that match labels and nodes that match entities. xref fields will result in sub trees.
	 *
	 * @param labels to be matched
	 * @param parent so far in the tree to allow for recursion
	 * @return tree of paths matching labels.
	 * @throws MolgenisModelException
	 */
	protected void getXrefLabelTree(List<String> labels, SimpleTree<?> parent) throws MolgenisModelException
	{
		getXrefLabelTree(labels, parent, true);
	}

	/**
	 * Creates a tree with leafs that match labels and nodes that match entities. xref fields will result in sub trees.
	 *
	 * @param labels to be matched
	 * @param parent so far in the tree to allow for recursion
	 * @return tree of paths matching labels.
	 * @throws MolgenisModelException
	 */
	protected void getXrefLabelTree(List<String> labels, SimpleTree<?> parent, boolean useJavaNames)
			throws MolgenisModelException
	{
		for (Field f : this.getXrefEntity().getAllFields())
		{
			String name = parent.getName() + "_" + getJavaName(f.getName(), useJavaNames);
			if (!(f.getType() instanceof XrefField) && !(f.getType() instanceof MrefField))
			{
				if (labels.contains(name))
				{
					Tree<SimpleTree<?>> leaf = new SimpleTree<SimpleTree<?>>(name, parent);
					leaf.setValue(f);
					// break;
				}
			}
		}

		for (Field f : this.getXrefEntity().getAllFields())
		{
			String name = parent.getName() + "_" + f.getName();

			if (f.getType() instanceof XrefField)
			{
				// check for cyclic relations
				// FIXME check for indirect cyclic relations or limit nesting
				// arbitrarily
				if (!f.getXrefEntity().equals(this.getXrefEntity()))
				{

					SimpleTree<SimpleTree<?>> node = new SimpleTree<SimpleTree<?>>(name, null);
					// get fields from subtree
					f.getXrefLabelTree(labels, node);
					// only attach the node if it leads to a label
					for (SimpleTree<?> child : node.getAllChildren())
					{
						if (labels.contains(child.getName()))
						{
							node.setParent(parent);
							node.setValue(f);
							break;
						}
					}
				}
			}
		}
	}

	public List<Field> getXrefLabelPath(String label) throws MolgenisModelException
	{
		return this.allPossibleXrefLabels().get(label);
	}

	public List<Field> getXrefLabels() throws MolgenisModelException
	{
		List<Field> result = new ArrayList<Field>();

		for (String label : getXrefLabelNames())
		{
			// absolute name
			if (label.contains("."))
			{
				result.add(this.getEntity().getModel().findField(label));
			} // path through xref to another field, path separated by _
			// caveat is fieldnames with '_' in the name
			// solution is to match against all possible xref_label
			// candidates
			else if (label.contains("_"))
			{
				// match agains all known labels
				Map<String, List<Field>> candidates = this.allPossibleXrefLabels();
				for (Entry<String, List<Field>> entry : candidates.entrySet())
				{
					String key = entry.getKey();
					if (key.toLowerCase().equals(label.toLowerCase()))
					{
						List<Field> value = entry.getValue();
						result.add(value.get(value.size() - 1));
					}
				}
			} // local name
			else
			{
				Field target = this.getEntity().getModel().findField(this.getXrefEntity().getName() + "." + label);
				result.add(new Field(target));
			}
		}
		return result;
	}

	// FIXME consistency: check if this is a xref field
	public List<String> getXrefLabelsTemp() throws MolgenisModelException
	{
		if (xref_labels == null || xref_labels.size() == 0)
		{
			if (this.getXrefEntity() == null)
			{
				throw new MolgenisModelException(
						"Cannot find xref_entity='" + getXrefEntityName() + "' for " + getEntity().getName() + "."
								+ getName());
			}
			if (this.getXrefEntity().getXrefLabels() != null)
			{
				return this.getXrefEntity().getXrefLabels();
			}
			else
			{
				return Arrays.asList(new String[] { this.xref_field });
			}
		}
		return xref_labels;
	}

	/**
	 * Gets the name of the link-table when this field is a XREF_MULTIPLE. When this field is not of type
	 * Type.XREF_MULTIPLE an exception is raised.
	 *
	 * @return The name of the linktable.
	 * @throws Exception When this field is not of type Type.XREF_MULTIPLE
	 */
	public String getMrefName()
	{
		return this.mref_name;
	}

	/**
	 * Sets the name of the link-table when this field is a XREF_MULTIPLE. When this field is not of type
	 * Type.XREF_MULTIPLE an exception is raised.
	 *
	 * @param linktable The name of the linktable.
	 * @throws Exception When this field is not of type Type.XREF_MULTIPLE
	 */
	public void setMrefName(String linktable)
	{
		this.mref_name = linktable;
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

	public void setUserData(Object obj)
	{
		user_data = obj;
	}

	public Object getUserData()
	{
		return user_data;
	}

	public String getDefaultCode()
	{
		return default_code;
	}

	public void setDefaultCode(String default_code)
	{
		this.default_code = default_code;
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
		String str = "Field(";

		// entity
		str += "entity=" + entity.getName();

		// name/label
		str += ", name=" + name;

		// type
		str += ", type=" + type;
		if (type instanceof StringField)
		{
			str += "[" + varchar_length + "]";
		}
		else if (type instanceof XrefField || type instanceof MrefField)
		{
			try
			{
				str += "[" + this.getXrefEntityName() + "." + this.getXrefFieldName() + "]";
			}
			catch (MolgenisModelException e)
			{
				e.printStackTrace();
			}
		}
		if (type instanceof MrefField)
		{
			str += ", mref_name=" + this.mref_name + ", mref_localid=" + this.mref_localid + ", mref_remoteid="
					+ this.mref_remoteid;
		}
		if (type instanceof XrefField || type instanceof MrefField)
		{
			str += ", xref_label=" + toCsv(this.xref_labels);
		}

		// settings
		str += ", auto=" + auto;
		str += ", nillable=" + nillable;
		str += ", readonly=" + readonly;

		// default
		str += ", default=" + default_value;

		if (this.enum_options != null)
		{
			str += ", enum_options=" + this.enum_options;
		}

		// closure
		str += ")";

		return str;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 *
	 * @param obj The reference object with which to compare.
	 * @return True if this object is the same as the obj argument, false otherwise.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof Field)
		{
			return name.equals(((Field) obj).getName());
		}

		return false;
	}

	/**
	 * Returns a hash code value for the Field. This hash-code is used for quick searching in a vector of fields.
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
	private Entity entity;
	/**
	 * The type of this field.
	 */
	private FieldType type;
	/**
	 * The name of this field, which needs to be unique for the associated entity.
	 */
	private String name;
	/**
	 * The label of this field, which is used for the user interface.
	 */
	private String label;
	/**
	 * Whether this field is auto-assigned by the database.
	 */
	private boolean auto;
	/**
	 * Whether this field can be NULL in the database.
	 */
	private boolean nillable;
	/**
	 * Whether this field is read-only.
	 */
	private boolean hidden;
	/**
	 * Whether this field is hidden.
	 */
	private boolean readonly;
	private boolean aggregateable;
	/**
	 * The string that should be set as the default value (is passed to the database ...)
	 */
	private String default_value = null;
	/**
	 * A short description of this field.
	 */
	private String description;
	private String default_code;
	/**
	 * When this field a of type Type.ENUM, this vector contains the options
	 */
	private Vector<String> enum_options;
	/**
	 * When this field is of type Type.VARCHAR, this indicates the maximum length of the string.
	 */
	private int varchar_length;
	/**
	 * When this field is of type Type.XREF_SINGLE or Type.XREF_MULTIPLE, this is the name of the entity it is
	 * referencing.
	 */
	private String xref_entity;
	/**
	 * When this field is of type Type.XREF_SINGLE or Type.XREF_MULTIPLE, this is the name of the field of the entity it
	 * is referencing.
	 */
	private String xref_field;
	/**
	 * When this field is of type Type.XREF_SINGLE or Type.XREF_MULTIPLE, this is the label of the reference.
	 */
	private List<String> xref_labels;
	/**
	 * Boolean to indicate cascading delete
	 */
	private boolean xref_cascade = false;
	/**
	 * Boolean to indicate cascading delete
	 */
	private String jpaCascade = null;
	/**
	 * When this field is of type Type.XREF_MULTIPLE, this is the name of the link-table.
	 */
	private String mref_name;
	private String mref_localid;
	private String mref_remoteid;
	private boolean filter;
	private String filtertype;
	private String filterfield;
	private String filtervalue;
	/** */
	private boolean system;
	/**
	 * Contains a pointer to some user-data.
	 */
	private Object user_data;
	/**
	 * Used to <annotation>
	 */
	private String annotations;
	/**
	 * Used for serialization purposes.
	 */
	private static final long serialVersionUID = -1879739243713730190L;
	private String tableName;
	private Long minRange;
	private Long maxRange;

	public String getAnnotations()
	{
		return annotations;
	}

	public void setAnnotations(String annotations)
	{
		this.annotations = annotations;
	}

	public boolean hasFilter()
	{
		return filter;
	}

	public void setFilter(boolean filter)
	{
		this.filter = filter;
	}

	public String getFilterfield()
	{
		return filterfield;
	}

	public void setFilterfield(String filterfield)
	{
		this.filterfield = filterfield;
	}

	public String getFiltertype()
	{
		return filtertype;
	}

	public void setFiltertype(String filtertype)
	{
		this.filtertype = filtertype;
	}

	public String getFiltervalue()
	{
		return filtervalue;
	}

	public void setNillable(boolean nillable)
	{
		this.nillable = nillable;
	}

	public void setFiltervalue(String filtervalue)
	{
		this.filtervalue = filtervalue;
	}

	public void setEntity(Entity entity)
	{
		this.entity = entity;
	}

	// FIXME rename to setXrefFieldName
	public void setXrefField(String xrefField)
	{
		this.xref_field = xrefField;

	}

	public void setXrefLabelNames(List<String> labelNames)
	{
		this.xref_labels = labelNames;

	}

	public Long getMinRange()
	{
		return minRange;
	}

	public void setMinRange(Long minRange)
	{
		this.minRange = minRange;
	}

	public Long getMaxRange()
	{
		return maxRange;
	}

	public void setMaxRange(Long maxRange)
	{
		this.maxRange = maxRange;
	}

	public Map<String, List<Field>> allPossibleXrefLabels() throws MolgenisModelException
	{
		if (!(this.getType() instanceof XrefField) && !(this.getType() instanceof MrefField))
		{
			throw new MolgenisModelException("asking xref labels for non-xref field");
		}

		Map<String, List<Field>> result = new LinkedHashMap<String, List<Field>>();
		for (Unique key : getXrefEntity().getAllKeys())
		{
			for (Field f : key.getFields())
			{
				if (f.getType() instanceof XrefField || f.getType() instanceof MrefField)
				{
					f = getXrefEntity().getAllField(f.getName());

					Map<String, List<Field>> subpaths = f.allPossibleXrefLabels();
					for (Entry<String, List<Field>> pair : subpaths.entrySet())
					{
						List<Field> path = pair.getValue();
						path.add(0, f);
						String label = f.getName() + "_" + pair.getKey();
						result.put(label, path);

					}
				}
				else
				{
					List<Field> path = new ArrayList<Field>();
					path.add(f);
					result.put(f.getName(), path);
				}
			}
		}

		return result;
	}

	public Integer getLength() throws MolgenisModelException
	{
		if (this.getType() instanceof StringField)
		{
			return this.getVarCharLength();
		}
		return null;
	}

	public synchronized boolean isXrefCascade()
	{
		return xref_cascade;
	}

	public synchronized void setXrefCascade(boolean xrefCascade)
	{
		xref_cascade = xrefCascade;
	}

	public String getJpaCascade()
	{
		return jpaCascade;
	}

	public void setJpaCascade(String jpaCascade)
	{
		this.jpaCascade = jpaCascade;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public String getTableName()
	{
		return this.tableName;
	}

	// FIXME database specific: delete method or move to utility class
	public String getSqlName()
	{
		if (StringUtils.isNotEmpty(this.tableName))
		{
			return StringUtils.capitalize(this.tableName) + "." + this.name;
		}
		else
		{
			return this.name;
		}
	}

	/**
	 * If this a unique field? If this field is part of a compound key it will return false
	 *
	 * @return
	 * @throws MolgenisModelException
	 */
	public boolean isUnique() throws MolgenisModelException
	{
		for (Unique unique : entity.getUniqueKeysWithoutPk())
		{
			if ((unique.getFields().size() == 1) && unique.getFields().get(0).getName().equals(name))
			{
				return true;
			}
		}

		return false;
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
	 * Convert string with first character to uppercase.
	 *
	 * @param string
	 * @return string with first character in uppercase.
	 */
	public static String firstToUpper(String string)
	{
		if (string == null) return " NULL ";
		if (string.length() > 0) return string.substring(0, 1).toUpperCase() + string.substring(1);
		else return " ERROR[STRING EMPTY] ";
	}

	private String getJavaName(String name, boolean doFirstToUpper)
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
}
