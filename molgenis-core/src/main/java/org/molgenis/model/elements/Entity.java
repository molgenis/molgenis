/**
 * File: invengine_generate/meta/Entity.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 */

package org.molgenis.model.elements;

// jdk
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.model.MolgenisModelException;

/**
 * Describes a database-entity (or table).
 * 
 * @author RA Scheltema
 * @author MA Swertz
 */
public class Entity extends DBSchema implements Record
{
	/** Convenience variable for retrieving the primary key from the keys */
	public static final int PRIMARY_KEY = 0;

	private static final Logger logger = Logger.getLogger(Entity.class.getSimpleName());
	// member variables
	/** namespace, e.g. used for package name */
	private String namespace;
	/** The description of this entity as entered in the meta-file */
	private String description;
	/** The label which users will see in the user-interface */
	private String label;
	/** Indicates whether this entity is system-specific */
	private boolean system;
	/** List containing all the fields of this entity */
	private Vector<Field> fields;
	/** */
	private Vector<String> parents;
	/** The name of the entity this entity is implementing */
	private Vector<String> implements_parents = new Vector<String>();
	/** List containing all the indices of this entity */
	private Vector<Index> indices;
	/**
	 * List containing all the unique fields of this entity (index 0 is the
	 * primary key)
	 */
	private Vector<Unique> unique_fields = new Vector<Unique>();
	/** higly experimental: allows to add lazy load navigation to objects */
	private Vector<Field> references = new Vector<Field>();
	/**
	 * Boolean that indicates whether this entity is to be instantiated
	 * (abstract, interface).
	 */
	private boolean abstract_type;
	/** Boolean indicate whether this is an association table */
	private boolean association_type;
	/** Used for serialization purposes */
	private static final long serialVersionUID = 3863969722351309896L;

	/** Trigger type */
	private String decorator;

	/** Default label(s) to be used for Xrefs */
	private List<String> xrefLabels;

	private boolean imported;

	// constructor(s)
	/**
	 * Default constructor. With this constructor all the needed information of
	 * an entity is set. Please note that the name needs to be unique in the
	 * tree.
	 * 
	 * @param name
	 *            The name of the entity.
	 * @param parent
	 *            Pointer to the DBSchema this entity belongs to.
	 */
	public Entity(String name, DBSchema parent)
	{
		this(name, name, parent);
	}

	/**
	 * Default constructor. With this constructor all the needed information of
	 * an entity is set. Please note that the name needs to be unique in the
	 * tree.
	 * 
	 * @param name
	 *            The name of the entity.
	 * @param label
	 *            The label of the entity.
	 * @param parent
	 *            Pointer to the DBSchema this entity belongs to.
	 */
	public Entity(String name, String label, DBSchema parent)
	{
		super(name, parent, parent.getModel());

		this.label = label;
		this.fields = new Vector<Field>();

		this.indices = new Vector<Index>();
		this.unique_fields = new Vector<Unique>();
		this.parents = new Vector<String>();

		this.description = "";

		this.system = false;
	}

	/**
	 * Default constructor. With this constructor all the needed information of
	 * an entity is set. Please note that the name needs to be unique in the
	 * tree.
	 * 
	 * @param name
	 *            The name of the entity.
	 * @param label
	 *            The label of the entity.
	 * @param parent
	 *            Pointer to the DBSchema this entity belongs to.
	 * @param system
	 *            Indicates whether this entity is system-specific
	 */
	public Entity(String name, String label, DBSchema parent, boolean system)
	{
		super(name, parent, parent.getModel());

		// this.name = name;
		this.label = label;
		this.fields = new Vector<Field>();
		this.indices = new Vector<Index>();
		this.unique_fields = new Vector<Unique>();
		this.parents = new Vector<String>();

		this.description = "";

		this.system = system;
	}

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }
	/**
	 * Returns the label of this entity.
	 * 
	 * @return The label of the entity.
	 */
	@Override
	public String getLabel()
	{
		if (label == null) return getName();
		return this.label;
	}

	/**
	 * Set the label of this entity.
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * Returns whether this entity is system-specific.
	 * 
	 * @return True when the entity is system-specific, false otherwise.
	 */
	public boolean isSystem()
	{
		return system;
	}

	/**
	 * Returns whether this entry has NO parent AND whether it has children'
	 * removed.
	 */
	public boolean isRootAncestor()
	{
		return this.getParents().size() == 0 && this.getDescendants().size() > 0;
	}

	/**
	 * Returns whether this entry is abstract
	 */
	public boolean isAbstract()
	{
		return this.abstract_type;
	}

	public void setAbstract(boolean is_abstract)
	{
		this.abstract_type = is_abstract;
	}

	public boolean isAssociation()
	{
		return association_type;
	}

	public void setAssociation(boolean association_type)
	{
		this.association_type = association_type;
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

	/**
	 * 
	 */
	@Override
	public boolean hasXRefs()
	{
		for (Field f : fields)
		{
			if (f.getType() instanceof MrefField || f.getType() instanceof XrefField) return true;
		}

		return false;
	}

	/**
	 * With this method all the parents of this entity can be set in one call.
	 * The parents are the base-entities, this entity inherits from.
	 * 
	 * @param parents
	 *            List containing the parents of this entity.
	 */
	public void setParents(Vector<String> parents)
	{
		this.parents = parents;

		// if (parents.size() > 0)
		// {
		// Vector<String> enumOptions = new Vector<String>();
		// enumOptions.add(getName());
		// Field type_field = new Field(this, Field.Type.ENUM, Field.TYPE_FIELD,
		// Field.TYPE_FIELD, true, false, true, null);
		// type_field.setDescription("Subtypes of " + getName() +
		// ". Have to be set to allow searching");
		// type_field.setEnumOptions(enumOptions);
		// type_field.setSystem(true);
		// this.fields.add(type_field);
		// }

	}

	/**
	 * Returns all the parents of this entity.
	 * 
	 * @return List with the parents
	 */
	@Override
	public Vector<String> getParents()
	{
		return parents;
	}

	/**
	 * Returns the root of the entity hierarchy this entity belongs to.
	 * 
	 * @return Entity
	 */
	public Entity getRootAncestor()
	{
		if (this.getAncestor() != null)
		{
			return this.getAncestor().getRootAncestor();
		}
		else
		{
			return this;
		}
	}

	public boolean isParent(String parent)
	{
		return this.parents.contains(parent);
	}

	public boolean hasAncestor()
	{
		return parents.size() > 0;
	}

	public boolean isAncestor(String entity)
	{
		if (getAllAncestors().contains(get(entity)))
		{
			return true;
		}
		return false;
	}

	public Entity getAncestor()
	{
		if (parents.size() > 0)
		{
			return (Entity) this.getRoot().getChild(parents.firstElement());
		}
		return null;
	}

	public Vector<Entity> getAllAncestors()
	{
		Vector<Entity> ancestors = new Vector<Entity>();
		if (getAncestor() != null)
		{
			ancestors.addAll(getAncestor().getAllAncestors());
			ancestors.add(getAncestor());
		}

		return ancestors;
	}

	/**
	 * Get the subclasses of this entity.
	 */
	public Vector<Entity> getDescendants()
	{
		Vector<Entity> descendants = new Vector<Entity>();
		// get the model
		for (DBSchema element : (this.getRoot()).getAllChildren())
		{
			if (element.getClass().equals(Entity.class))
			{
				if (((Entity) element).hasAncestor()
						&& ((Entity) element).getAncestor().getName().equals(this.getName()))
				{
					descendants.add((Entity) element);
				}
			}
		}

		return descendants;
	}

	public Vector<Entity> getAllDescendants()
	{
		Vector<Entity> all_descendants = new Vector<Entity>();
		for (Entity descendant : getDescendants())
		{
			all_descendants.addAll(descendant.getAllDescendants());
			all_descendants.add(descendant);
		}

		return all_descendants;
	}

	public boolean hasDescendants()
	{
		return this.getDescendants().size() > 0;
	}

	public boolean hasImplements()
	{
		return implements_parents.size() > 0;
	}

	public Vector<String> getImplementsNames()
	{
		return implements_parents;
	}

	public Vector<Entity> getImplements() throws MolgenisModelException
	{
		Vector<Entity> implements_entities = new Vector<Entity>();

		for (String iface : this.implements_parents)
		{
			Entity iface_entity = (Entity) getParent().get(iface);
			if (iface_entity == null) throw new MolgenisModelException("interface '" + iface
					+ "' is undefined for entity " + this.getName());
			implements_entities.add(iface_entity);
		}
		return implements_entities;
	}

	public Vector<Entity> getAllImplements() throws MolgenisModelException
	{
		Vector<Entity> implements_entities = new Vector<Entity>();

		for (String iface : this.implements_parents)
		{
			Entity iface_entity = (Entity) getParent().get(iface);
			if (iface_entity == null) throw new MolgenisModelException("interface " + iface
					+ " is undefined in entity " + this.getName());

			implements_entities.addAll(iface_entity.getAllImplements());
			implements_entities.add(iface_entity);
		}
		return implements_entities;
	}

	public void setImplements(String... implements_parents)
	{
		this.setImplements(new Vector<String>(Arrays.asList(implements_parents)));
	}

	public void setImplements(Vector<String> implements_parents)
	{
		this.implements_parents = implements_parents;
	}

	/**
	 * @param f
	 */
	public void addReference(Field f)
	{
		references.add(f);
	}

	public Vector<Field> getReferences()
	{
		return references;
	}

	// field access methods
	/**
	 * Adds the given field to the list of fields associated with this entity.
	 * When a field with the same name is already present in this entity an
	 * exception is thrown (field-names need to be unique).
	 * 
	 * @param field
	 *            Pointer to the field that needs to be added.
	 * @throws Exception
	 *             When a field with the same name is already present.
	 */
	public void addField(Field field) throws MolgenisModelException
	{
		this.addField(null, field);
	}

	/**
	 * Adds the given field to the list of fields associated with this entity.
	 * When a field with the same name is already present in this entity an
	 * exception is thrown (field-names need to be unique).
	 * 
	 * @param field
	 *            Pointer to the field that needs to be added.
	 * @throws Exception
	 *             When a field with the same name is already present.
	 */
	public void addField(Integer pos, Field field) throws MolgenisModelException
	{
		if (fields.contains(field))
		{
			throw new MolgenisModelException("Duplicate Field with name " + field.getName() + "  in entity "
					+ this.getName());
		}
		if (pos != null) fields.add(pos, field);
		else
			fields.add(field);
	}

	public void removeField(Field field)
	{
		fields.remove(field);
	}

	/**
	 * Returns a vector with all the fields associated with this entity.
	 * 
	 * @return All the fields associated with this entity.
	 * @throws MolgenisModelException
	 */
	@Override
	public List<Field> getFields() throws MolgenisModelException
	{
		return getFields(false, false, true);
	}

	/**
	 * Returns a vector with all the fields associated with this entity.
	 * 
	 * @param required
	 *            if required == true than returns only fields that are required
	 *            (not nillable/null) else returns all fields
	 * @param recursive
	 *            get also field from super classes
	 * @param systemField
	 *            system field, like __type and id
	 * @param implementing
	 *            field that this object implements (also done recusively if
	 *            recusive = true)
	 * @return All the fields associated with this entity.
	 * @throws MolgenisModelException
	 */
	public List<Field> getFields(boolean required, boolean recursive, boolean systemField, boolean implementing)
			throws MolgenisModelException
	{
		// use map to ensure we can override fields in subclasses
		Map<String, Field> result = new LinkedHashMap<String, Field>();

		// List<Field> result = new ArrayList<Field>();
		for (Field f : fields)
		{
			if (f.isSystem())
			{
				if (systemField)
				{
					result.put(f.getName(), f);
				}
				// else ignore
			}
			else if (f.isNillable())
			{
				if (!required)
				{
					result.put(f.getName(), f);
				}
			}
			else
			{
				result.put(f.getName(), f);
			}
		}

		if (implementing)
		{
			for (Entity implEntity : this.getImplements())
			{
				for (Field f : implEntity.getFields(required, recursive, systemField, implementing))
				{
					if (!result.containsKey(f.getName()))
					{
						result.put(f.getName(), f);
					}
				}
			}
		}

		if (recursive && hasAncestor())
		{
			for (Field f : getAncestor().getFields(required, recursive, systemField, implementing))
			{
				if (!result.containsKey(f.getName()))
				{
					result.put(f.getName(), f);
				}
			}
		}

		return new ArrayList<Field>(result.values());
	}

	public List<Field> getFields(boolean required, boolean recursive, boolean systemField)
			throws MolgenisModelException
	{
		return getFields(required, recursive, systemField, true);
	}

	/**
	 * Get fields for this entity as well as from the interfaces it implements.
	 * 
	 * @return vector of fields implemented by this entity or its interfaces
	 * @throws MolgenisModelException
	 */
	public Vector<Field> getImplementedFields() throws MolgenisModelException
	{
		// use map so we can override fields in subclasses
		Map<String, Field> all_fields = new LinkedHashMap<String, Field>();

		// first fields of the interfaces
		for (Entity iface : this.getAllImplements())
		{
			Vector<Field> ifaceFields = new Vector<Field>(iface.getAllFields());
			// Collections.copy(ifaceFields, iface.getAllFields());
			for (Field ifaceField : ifaceFields)
			{
				// ifaceField.setEntity(this);
				all_fields.put(ifaceField.getName().toLowerCase(), ifaceField);
			}
		}

		// then of self...
		for (Field f : getFields())
		{
			all_fields.put(f.getName().toLowerCase(), f);
		}

		// clean all abstract entities
		if (!this.isAbstract()) for (Entry<String, Field> entry : all_fields.entrySet())
		{
			if (entry.getValue().getEntity().isAbstract())
			{
				// copy the field and change entity
				Field value = new Field(entry.getValue());
				value.setEntity(this);
				entry.setValue(value);
			}
		}

		return new Vector<Field>(all_fields.values());
	}

	public boolean hasSuperclassField(String fieldname) throws MolgenisModelException
	{
		Vector<Field> fields = getAncestor().getAllFields();
		for (Field f : fields)
		{
			if (f.getName().equals(fieldname))
			{
				return true;
			}
		}
		return false;
	}

	public Vector<Field> getInheritedFields() throws MolgenisModelException
	{
		Map<String, Field> all_fields = new LinkedHashMap<String, Field>();

		// second fields of the interfaces
		for (Entity iface : this.getImplements())
		{
			Vector<Field> ifaceFields = new Vector<Field>(iface.getAllFields());
			// Collections.copy(ifaceFields, iface.getAllFields());
			for (Field ifaceField : ifaceFields)
			{
				// ifaceField.setEntity(this);
				all_fields.put(ifaceField.getName().toLowerCase(), ifaceField);
			}
		}

		// third the fields of the superclass
		if (getAncestor() != null)
		{
			for (Field f : getAncestor().getAllFields())
			{
				all_fields.put(f.getName().toLowerCase(), f);
			}
		}

		// clean all abstract entities
		if (!this.isAbstract()) for (Entry<String, Field> entry : all_fields.entrySet())
		{
			if (entry.getValue().getEntity().isAbstract())
			{
				// copy the field and change entity
				Field value = new Field(entry.getValue());
				value.setEntity(this);
				entry.setValue(value);
			}
		}

		// skip self...
		return new Vector<Field>(all_fields.values());

	}

	/**
	 * @throws MolgenisModelException
	 */
	public Vector<Field> getAllFields() throws MolgenisModelException
	{
		Map<String, Field> all_fields = new LinkedHashMap<String, Field>();

		// second the fields of the superclass
		if (getAncestor() != null)
		{
			for (Field f : getAncestor().getAllFields())
			{
				all_fields.put(f.getName().toLowerCase(), f);
			}
		}

		// first fields of the interfaces
		for (Entity iface : this.getImplements())
		{
			Vector<Field> ifaceFields = iface.getAllFields();
			for (Field ifaceField : ifaceFields)
			{
				all_fields.put(ifaceField.getName().toLowerCase(), ifaceField);
			}
		}

		// third of self...
		for (Field f : getFields())
		{
			// TODO:
			// Find out why the if-statement below is commented out (Joris
			// doens't remember)
			// We found out that it does NOT cause the lock-wait-timeouts on
			// Hudson, as suspected by Joeri and Danny
			// if (!all_fields.containsKey(f.getName().toLowerCase()))
			all_fields.put(f.getName().toLowerCase(), f);
		}

		// replace all abstract entity references, unless self abstract
		if (!this.isAbstract()) for (Entry<String, Field> entry : all_fields.entrySet())
		{
			if (entry.getValue().getEntity().isAbstract())
			{
				// copy the field and change entity
				Field value = new Field(entry.getValue());
				value.setEntity(this);
				entry.setValue(value);
			}
		}

		return new Vector<Field>(all_fields.values());
	}

	public Vector<Field> getAllUpdateFields() throws MolgenisModelException
	{
		Vector<Field> all_fields = getAllFields();
		Vector<Field> all_update_fields = new Vector<Field>();
		for (Field f : all_fields)
		{
			if (!(f.getType() instanceof MrefField))
			{
				all_update_fields.add(f);
			}
		}

		return all_update_fields;
	}

	/**
	 * Returns a vector with all the fields that are actualy located in this
	 * entity. For an xref_multiple the entries are located in a couple-table.
	 * 
	 * @return All the fields local for the entity.
	 * @throws MolgenisModelException
	 */
	public Vector<Field> getLocalFields() throws MolgenisModelException
	{
		Vector<Field> local_fields = new Vector<Field>();

		for (Field f : getFields())
		{
			if (!(f.getType() instanceof MrefField))
			{
				local_fields.add(f);
			}
		}

		return local_fields;
	}

	/**
	 * Returns a vector with all the fields that are actualy located in this
	 * entity. For an xref_multiple the entries are located in a couple-table.
	 * 
	 * @return All the fields local for the entity.
	 * @throws MolgenisModelException
	 */
	public Vector<Field> getAllLocalFields() throws MolgenisModelException
	{
		Vector<Field> local_fields = new Vector<Field>();

		for (Field f : getAllFields())
		{
			if (!(f.getType() instanceof MrefField))
			{
				local_fields.add(f);
			}
		}

		return local_fields;
	}

	/**
	 * Returns a vector with all the fields that are located in other tables,
	 * but belong to this entity (xref_multiple).
	 * 
	 * @return All the fields external for the entity.
	 * @throws MolgenisModelException
	 */
	public Vector<Field> getRemoteFields() throws MolgenisModelException
	{
		Vector<Field> local_fields = new Vector<Field>();

		for (Field f : getFields())
		{
			if (f.getType() instanceof MrefField)
			{
				local_fields.add(f);
			}
		}

		return local_fields;
	}

	/**
	 * @throws MolgenisModelException
	 */
	public List<Field> getSystemFields(boolean all) throws MolgenisModelException
	{
		List<Field> the_fields;
		List<Field> system_fields = new ArrayList<Field>();

		if (!all) the_fields = getFields();
		else
			the_fields = getAllFields();

		for (Field f : the_fields)
		{
			if (f.isSystem())
			{
				system_fields.add(f);
			}
		}

		return system_fields;
	}

	/**
	 * Returns all the fields belonging to this entity, which have not set the
	 * system-property.
	 * 
	 * @return All the non-system fields for the entity.
	 * @throws MolgenisModelException
	 */
	public List<Field> getNonSystemFields(boolean all) throws MolgenisModelException
	{
		List<Field> the_fields;
		List<Field> nonsystem_fields = new ArrayList<Field>();

		if (!all) the_fields = getFields();
		else
			the_fields = getAllFields();

		for (Field f : the_fields)
		{
			if (!f.isSystem())
			{
				nonsystem_fields.add(f);
			}
		}

		return nonsystem_fields;
	}

	public Vector<Field> getAllAddFields() throws MolgenisModelException
	{
		Vector<Field> local_fields = new Vector<Field>();

		for (Field f : getAllFields())
		{
			if (!(f.getType() instanceof XrefField)
					&& !(f.getType() instanceof IntField && f.isAuto() && f.getEntity() == this))
			// TODO: fix automatic fields
			// MAJOR error, arghhhh!!! &&
			// !getKeyFields(PRIMARY_KEY).contains(f))
			{
				local_fields.add(f);
			}
		}

		return local_fields;
	}

	// TODO: Remove this, specialy made for hsql. Think up of something ...
	// anything!!!!
	public Vector<Field> getAddFields() throws MolgenisModelException
	{
		Vector<Field> local_fields = new Vector<Field>();

		for (Field f : getFields())
		{
			if (!(f.getType() instanceof MrefField) && !(f.getType() instanceof IntField && f.isAuto()))
			// TODO: fix automatic fields
			// MAJOR error, arghhhh!!! &&
			// !getKeyFields(PRIMARY_KEY).contains(f))
			{
				local_fields.add(f);
			}
		}

		return local_fields;
	}

	/**
	 * Returns a vector with all the fields associated with this entity and are
	 * of the given type.
	 * 
	 * @return All the fields associated with this entity with the given type.
	 * @throws MolgenisModelException
	 */
	public Vector<Field> getFieldsOf(FieldType type) throws MolgenisModelException
	{
		Vector<Field> results = new Vector<Field>();

		for (Field field : getFields())
		{
			if (field.getType().getClass() == type.getClass())
			{
				results.add(field);
			}
		}

		return results;
	}

	public Vector<Field> getAllFieldsOf(FieldType type) throws MolgenisModelException
	{
		Vector<Field> results = new Vector<Field>();

		for (Field field : this.getAllFields())
		{
			if (field.getType().getClass().equals(type.getClass()))
			{
				results.add(field);
			}
		}

		return results;

	}

	public Vector<Field> getAllFieldsOf(String typeName) throws MolgenisModelException
	{
		return this.getAllFieldsOf(MolgenisFieldTypes.getType(typeName));

	}

	public Vector<Field> getImplementedFieldsOf(FieldType type) throws MolgenisModelException
	{
		Vector<Field> results = new Vector<Field>();

		for (Field field : getImplementedFields())
		{
			if (field.getType().getClass().equals(type.getClass()))
			{
				results.add(field);
			}
		}

		return results;
	}

	public Vector<Field> getImplementedFieldsOf(String typeName) throws MolgenisModelException
	{
		return this.getImplementedFieldsOf(MolgenisFieldTypes.getType(typeName));
	}

	/**
	 * @throws MolgenisModelException
	 */
	public List<Field> getXRefFields() throws MolgenisModelException
	{
		Vector<Field> xref_fields = new Vector<Field>();

		for (Field field : getImplementedFields())
		{
			if (field.isSystem()) continue;
			if (field.getType() instanceof XrefField || field.getType() instanceof MrefField) xref_fields.add(field);
		}

		return xref_fields;
	}

	/**
	 * @throws MolgenisModelException
	 */
	public List<Field> getUserFields() throws MolgenisModelException
	{
		Vector<Field> xref_fields = new Vector<Field>();

		for (Field field : getFields())
		{
			if (field.isSystem()) continue;
		}

		return xref_fields;
	}

	/**
	 * Returns the field with the given name. When no field with the given name
	 * is found null is returned.
	 * 
	 * @param name
	 *            The name of the field to look for.
	 * @return The field with the given name.
	 * @throws MolgenisModelException
	 */

	public Field getField(String name, boolean required, boolean recursive, boolean systemFields)
			throws MolgenisModelException
	{
		for (Field field : getFields(required, recursive, systemFields))
		{
			if (name.equals(field.getName()))
			{
				return field;
			}
		}

		return null;
	}

	public Field getFieldRecusive(String name) throws MolgenisModelException
	{
		return getField(name, false, true, true);
	}

	public Field getField(String name) throws MolgenisModelException
	{
		return getField(name, false, false, true);
	}

	/**
	 * Returns the field with the given name from entity or any of its parents.
	 * When no field with the given name is found null is returned.
	 * 
	 * @param name
	 *            The name of the field to look for.
	 * @return The field with the given name.
	 * @throws MolgenisModelException
	 */
	public Field getAllField(String name) throws MolgenisModelException
	{
		for (Field field : getAllFields())
			if (name.equalsIgnoreCase(field.getName())) return field;
		logger.debug("couldn't find " + this.getName() + "." + name);

		return null;
	}

	// index access methods
	/**
	 * Adds the given index to the list of indices associated with this entity.
	 * When a Index with the same name is already present in this entity an
	 * exception is thrown (index-names need to be unique).
	 * 
	 * @param index
	 *            Pointer to the index that needs to be added.
	 * @throws Exception
	 *             When a index with the same name is already present.
	 */
	public void addIndex(Index index)// throws Exception
	{
		// if (indices.contains(index))
		// {
		// throw new Exception("Index with name " + index.getName() + " already
		// in entity "+this.getName());
		// }

		indices.add(index);
	}

	/**
	 * Returns a vector with all the indices associated with this entity.
	 * 
	 * @return All the indices associated with this entity.
	 * @throws MolgenisModelException
	 */
	public Vector<Index> getIndices() throws MolgenisModelException
	{
		Vector<Index> i = new Vector<Index>();
		i.addAll(indices);

		for (Entity iface : this.getImplements())
		{
			i.addAll(iface.getIndices());
		}
		return i;
	}

	/**
	 * @throws MolgenisModelException
	 */
	public Vector<Index> getAllIndices() throws MolgenisModelException
	{
		Vector<Index> all_indices = new Vector<Index>();

		if (parents.size() == 1)
		{
			Entity parent_entity = (Entity) getParent().get(parents.get(0));
			all_indices = parent_entity.getAllIndices();
		}
		for (Index i : getIndices())
		{
			if (!all_indices.contains(i)) all_indices.add(i);
		}

		return all_indices;
	}

	/**
	 * Returns the index with the given name. When no index with the given name
	 * is found null is returned.
	 * 
	 * @param name
	 *            The name of the index to look for.
	 * @return The index with the given name.
	 * @throws MolgenisModelException
	 */
	public Index getIndex(String name) throws MolgenisModelException
	{
		for (Index index : getIndices())
		{
			if (index.getName().equals(name))
			{
				return index;
			}
		}

		return null;
	}

	// key access methods
	/**
	 * Adds the given field to the key-list. The given field should be present
	 * in the field-list of this entity. The field is regarded as a key, meaning
	 * it does not need to be combined with other keys. When there no keys for
	 * this entity, the given key is regarded as the primary key, otherwise the
	 * key is a secondary key.
	 * 
	 * @param key
	 *            The field that is a key.
	 */
	public void addKey(String key, String description) // throws Exception
	{
		// if (fields.contains(key) == false)
		// {
		// throw new Exception("Entity does not contain field with name " +
		// key.getName() + ", so it cannot be made key.");
		// }

		unique_fields.add(new Unique(this, key, false, description));
	}

	/**
	 * Adds the given list of fields to the key-list. The given fields should be
	 * present in the field-list of the entity. The fields are regarded together
	 * as the key. When there no keys for this entity, the given key is regarded
	 * as the primary key, otherwise the key is a secondary key.
	 * 
	 * @param keys
	 *            The fields that combined form the key.
	 * @throws MolgenisModelException
	 */
	public void addKey(List<String> keys, boolean subclass, String description) throws MolgenisModelException
	{
		// for (String key : keys)
		// {
		// if (getAllField(key) == null)
		// {
		// throw new
		// MolgenisModelException("Entity does not contain field with name " +
		// key
		// + ", so it cannot be made key.");
		// }
		// }

		unique_fields.add(new Unique(this, keys, subclass, description));
	}

	public Vector<Unique> getAllKeys() throws MolgenisModelException
	{
		Vector<Unique> all_keys = new Vector<Unique>();

		if (getAncestor() != null)
		{
			all_keys.addAll(getAncestor().getAllKeys());
		}
		for (Unique u : this.getKeys())
		{
			if (!all_keys.contains(u))
			{
				all_keys.add(u);
			}
		}

		return all_keys;
	}

	/**
	 * Returns a list with all the keys.
	 * 
	 * @return All the keys of this entity.
	 * @throws MolgenisModelException
	 */
	public Vector<Unique> getKeys() throws MolgenisModelException
	{
		Vector<Unique> result = new Vector<Unique>();

		// get primary key from parent
		if (hasAncestor())
		{
			Entity parent_entity = (Entity) getParent().get(this.parents.lastElement());
			if (parent_entity == null) throw new MolgenisModelException("Superclass " + this.parents.lastElement()
					+ " unknown for entity " + this.getName());
			if (parent_entity.getKeys().size() == 0) throw new MolgenisModelException(this.parents.lastElement()
					+ " or the interface it implements doesn't define primary key (unique,int,not null)");
			result.add(parent_entity.getKeys().firstElement());

		}

		// get other keys from implements
		if (hasImplements())
		{
			for (Entity e : getImplements())
			{
				// we need to rewrite the uniques to point to the right entity
				for (Unique u : e.getKeys())
				{
					Unique copy = new Unique(u);
					u.setEntity(this);
					if (!result.contains(copy))
					{
						result.add(copy);
					}
				}
			}
		}
		// get local keys
		for (Unique u : unique_fields)
		{
			if (!result.contains(u))
			{
				result.add(u);
			}
		}

		return result;
	}

	/**
	 * Returns the key at the given index. The first key in the list (index 0)
	 * is regarded as the primary key.
	 * 
	 * @return The key at the given index.
	 * @throws MolgenisModelException
	 */
	public Unique getKey(int index) throws IndexOutOfBoundsException, MolgenisModelException
	{
		if (index < 0 || index >= unique_fields.size())
		{
			throw new IndexOutOfBoundsException("No key was found for entity " + this.getName()
					+ " at the given index " + index + ".");
		}

		return getKeys().get(index);
	}

	/**
	 * Returns a list of the fields that make up the key at the given index. The
	 * first key in the list (index 0) is regarded as the primary key.
	 * 
	 * @return Vector with fields making up the key at the given index.
	 */
	public Vector<Field> getKeyFields(int index) throws MolgenisModelException
	{
		Vector<Field> fields = new Vector<Field>();
		if (getKeys().size() > index)
		{
			fields = getKeys().get(index).getFields();
		}
		else
		{
			logger.warn("[WARNING]: missing key " + index + " for entity " + this.getName());
		}

		return fields;
	}

	public List<Unique> getUniqueKeysWithoutPk() throws MolgenisModelException
	{
		List<Unique> result = new ArrayList<Unique>();

		if (hasImplements())
		{
			for (Entity e : getImplements())
			{
				// we need to rewrite the uniques to point to the right entity
				for (Unique u : e.getKeys())
				{
					if (u.getFields().get(0).isAuto())
					{
						continue;
					}
					Unique copy = new Unique(u);
					u.setEntity(this);
					if (!result.contains(copy))
					{
						result.add(copy);
					}
				}
			}
		}

		for (Unique u : unique_fields)
		{
			if (u.getFields().get(0).isAuto())
			{
				continue;
			}
			result.add(u);
		}
		return result;
	}

	// Object overloads
	/**
	 * Returns a string representation of the Entity.
	 * 
	 * @return The string-representation.
	 */
	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder("Entity(");
		strBuilder.append(getNamespace()).append('.').append(getName()).append(")\n(\n");
		for (Field field : fields)
			strBuilder.append(' ').append(field.toString()).append('\n');
		for (Unique unique : unique_fields)
			strBuilder.append(' ').append(unique.toString()).append('\n');
		for (Index index : indices)
			strBuilder.append(' ').append(index.toString()).append('\n');
		strBuilder.append(");");

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
		if (obj != null && obj instanceof Entity)
		{
			return getName().equals(((Entity) obj).getName());
		}

		return false;
	}

	/**
	 * Returns a hash code value for the Entity. This hash-code is used for
	 * quick searching in a vector of entities.
	 * 
	 * @return The hash-value for this field.
	 */
	@Override
	public int hashCode()
	{
		return getName().hashCode();
	}

	public String getDecorator()
	{
		return decorator;
	}

	public void setDecorator(String decorator)
	{
		this.decorator = decorator;
	}

	private Module module;

	public Module getModule()
	{
		return module;
	}

	public void setModule(Module module)
	{
		this.module = module;
	}

	public void setSystem(boolean isSystem)
	{
		this.system = isSystem;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}

	public Field getPrimaryKey() throws MolgenisModelException
	{
		if (hasAncestor())
		{
			return getAncestor().getPrimaryKey();
		}
		return this.getAllKeys().get(0).getFields().get(0);
	}

	public void setParents(String[] parents)
	{
		Vector<String> result = new Vector<String>();
		for (String parent : parents)
			result.add(parent);
		this.setParents(result);
	}

	public int getNumberOfReferencesTo(Entity e) throws MolgenisModelException
	{
		int count = 0;
		for (Field field : this.getXRefFields())
		{
			String xrefEntity = field.getXrefEntityName();
			if (xrefEntity != null && xrefEntity.equals(e.getName())) count++;
		}
		return count;
	}

	public int getNumberOfReferencesTo(Entity e, Field f) throws MolgenisModelException
	{
		int count = 0;
		for (Field field : this.getXRefFields())
		{
			String xrefEntity = field.getXrefEntityName();
			if (xrefEntity != null && xrefEntity.equals(e.getName()) && field.getName().equals(f.getName())) count++;
		}
		return count;
	}

	public int getNumberOfMrefTo(Entity e) throws MolgenisModelException
	{
		int count = 0;
		for (Field field : this.getFields())
		{
			if (field.isMRef())
			{
				String xrefEntity = field.getXrefEntityName();
				if (xrefEntity != null && xrefEntity.equals(e.getName())) count++;
			}
		}
		return count;
	}

	public int getNumberOfMrefTo(Entity e, Field f) throws MolgenisModelException
	{
		int count = 0;
		for (Field field : this.getFields())
		{
			if (field.isMRef())
			{
				String xrefEntity = field.getXrefEntityName();
				if (xrefEntity != null && xrefEntity.equals(e.getName()) && field.getName().equals(f.getName())) count++;
			}
		}
		return count;
	}

	public List<String> getXrefLabels() throws MolgenisModelException
	{
		// get from super class or interfaces
		if (xrefLabels == null)
		{
			if (this.hasAncestor() && this.getAncestor().getXrefLabels() != null) return this.getAncestor()
					.getXrefLabels();

			List<Entity> ifaces = new ArrayList<Entity>(this.getImplements());
			Collections.reverse(ifaces);
			for (Entity iface : ifaces)
			{
				if (iface.getXrefLabels() != null) return iface.getXrefLabels();
			}

			List<String> result = new ArrayList<String>();
			if (this.getKeys().size() > 0)
			{
				// use secondary keys, otherwise primary keys
				List<Field> keyFields = null;
				if (this.getKeys().size() > 1)
				{
					keyFields = this.getKeyFields(1);
				}
				else
				{
					keyFields = this.getKeyFields(0);
				}

				for (Field f : keyFields)
				{
					result.add(f.getName());
				}
				return result;
			}
		}

		return xrefLabels;
	}

	public void setXrefLabels(List<String> xrefLabels)
	{
		this.xrefLabels = xrefLabels;
	}

	@Override
	public Model getModel()
	{
		return this.getRoot().getModel();
	}

	public List<Entity> getDependencies() throws MolgenisModelException
	{
		List<Entity> result = new ArrayList<Entity>();
		for (Field f : getAllFields())
		{
			if (f.getType() instanceof XrefField)
			{
				if (!f.getXrefEntityName().equals(getName())) result.add(f.getXrefEntity());
			}
		}
		return result;
	}

	private Integer allocationSize = null;

	public void setAllocationSize(Integer allocationSize)
	{
		this.allocationSize = allocationSize;
	}

	public Integer getAllocationSize()
	{
		return this.allocationSize;
	}
}
