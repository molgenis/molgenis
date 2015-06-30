package org.molgenis.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisOptions;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.TextField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Field;
import org.molgenis.model.elements.Model;
import org.molgenis.model.elements.Module;
import org.molgenis.model.elements.Unique;
import org.molgenis.model.elements.View;
import org.molgenis.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MolgenisModelValidator
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisModelValidator.class);

	public static void validate(Model model, MolgenisOptions options) throws MolgenisModelException
	{
		LOG.debug("validating model and adding defaults:");

		// validate the model
		validateNamesAndReservedWords(model, options);
		validateExtendsAndImplements(model);

		if (options.object_relational_mapping.equals(MolgenisOptions.SUBCLASS_PER_TABLE))
		{
			addTypeFieldInSubclasses(model);
		}

		validateKeys(model);
		addXrefLabelsToEntities(model);
		validatePrimaryKeys(model);
		validateForeignKeys(model);
		validateViews(model);
		validateOveride(model);

		// enhance the model
		correctXrefCaseSensitivity(model);
		// if(!options.mapper_implementation.equals(MolgenisOptions.MapperImplementation.JPA))
		// {
		moveMrefsFromInterfaceAndCopyToSubclass(model);
		createLinkTablesForMrefs(model, options);
		// }
		copyDefaultXrefLabels(model);
		copyDecoratorsToSubclass(model);

		if (options.object_relational_mapping.equals(MolgenisOptions.CLASS_PER_TABLE))
		{
			addInterfaces(model);
		}

		copyFieldsToSubclassToEnforceConstraints(model);

		validateNameSize(model, options);

	}

	/**
	 * As mrefs are a linking table between to other tables, interfaces cannot be part of mrefs (as they don't have a
	 * linking table). To solve this issue, mrefs will be removed from interface class and copied to subclass.
	 * 
	 * @throws MolgenisModelException
	 */
	public static void moveMrefsFromInterfaceAndCopyToSubclass(Model model) throws MolgenisModelException
	{
		LOG.debug("copy fields to subclass for constrain checking...");

		// copy mrefs from interfaces to implementing entities
		// also rename the target from interface to entity
		for (Entity entity : model.getEntities())
		{
			for (Entity iface : entity.getImplements())
			{
				for (Field mref : iface.getFieldsOf(new MrefField()))
				{
					Field f = new Field(mref);
					f.setEntity(entity);

					String mrefName = entity.getName() + "_" + f.getName();
					if (mrefName.length() > 30)
					{
						mrefName = mrefName.substring(0, 25) + Integer.toString(mrefName.hashCode()).substring(0, 5);
					}
					f.setMrefName(mrefName);
					entity.addField(0, f);
				}
			}
		}

		// remove interfaces from entities
		for (Entity entity : model.getEntities())
		{
			if (entity.isAbstract()) for (Field mref : entity.getFieldsOf(new MrefField()))
			{
				entity.removeField(mref);
			}
		}
	}

	/**
	 * Subclasses can override fields of superclasses. This should only be used with caution! Only good motivation is to
	 * limit xref type.
	 */
	public static void validateOveride(Model model)
	{
		// TODO

	}

	public static void validateNameSize(Model model, MolgenisOptions options) throws MolgenisModelException
	{
		for (Entity e : model.getEntities())
		{
			// maximum num of chars in oracle table name of column is 30
			if (e.getName().length() > 30)
			{
				throw new MolgenisModelException(String.format("table name %s is longer than %d", e.getName(), 30));
			}
			for (Field f : e.getFields())
			{
				if (f.getName().length() > 30)
				{
					throw new MolgenisModelException(String.format("field name %s is longer than %d", f.getName(), 30));
				}
			}
		}
	}

	public static void validateHideFields(Model model) throws MolgenisModelException
	{
		for (org.molgenis.model.elements.Form form : model.getUserinterface().getAllForms())
		{
			List<String> hideFields = form.getHideFields();
			for (String fieldName : hideFields)
			{
				Entity entity = form.getEntity();
				Field field = entity.getAllField(fieldName);
				if (field == null)
				{
					throw new MolgenisModelException("error in hide_fields for form name=" + form.getName()
							+ ": cannot find field '" + fieldName + "' in form entity='" + entity.getName() + "'");
				}
				else
				{
					if (!form.getReadOnly() && field.isNillable() == false && !field.isAuto()
							&& field.getDefaultValue().equals(""))
					{

						LOG.warn("you can get trouble with hiding field '" + fieldName + "' for form name="
								+ form.getName()
								+ ": record is not null and doesn't have a default value (unless decorator fixes this!");
					}
				}
			}
		}
	}

	public static void addXrefLabelsToEntities(Model model) throws MolgenisModelException
	{
		for (Entity e : model.getEntities())
		{
			if (e.getXrefLabels() == null)
			{
				// still empty then construct from secondary key
				List<String> result = new ArrayList<String>();
				if (e.getAllKeys().size() > 1)
				{
					for (Field f : e.getAllKeys().get(1).getFields())
						result.add(f.getName());
					e.setXrefLabels(result);
				}

				// otherwise use primary key
				else if (e.getAllKeys().size() > 0)
				{
					for (Field f : e.getAllKeys().get(0).getFields())
						result.add(f.getName());
					e.setXrefLabels(result);
				}

				LOG.debug("added default xref_label=" + e.getXrefLabels() + " to entity=" + e.getName());

			}
		}

	}

	public static void validatePrimaryKeys(Model model) throws MolgenisModelException
	{
		for (Entity e : model.getEntities())
			if (!e.isAbstract())
			{
				if (e.getKeys().size() == 0) throw new MolgenisModelException("entity '" + e.getName()
						+ " doesn't have a primary key defined ");
			}
	}

	/**
	 * Default xref labels can come from: - the xref_entity (or one of its superclasses)
	 * 
	 * @param model
	 * @throws MolgenisModelException
	 */
	public static void copyDefaultXrefLabels(Model model) throws MolgenisModelException
	{
		for (Entity e : model.getEntities())
		{
			for (Field f : e.getFields())
			{
				if (f.getType() instanceof XrefField || f.getType() instanceof MrefField)
				{
					if (f.getXrefLabelNames().size() > 0 && f.getXrefLabelNames().get(0).equals(f.getXrefFieldName()))
					{
						Entity xref_entity = f.getXrefEntity();
						if (xref_entity.getXrefLabels() != null)
						{
							LOG.debug("copying xref_label " + xref_entity.getXrefLabels() + " from "
									+ f.getXrefEntityName() + " to field " + f.getEntity().getName() + "."
									+ f.getName());
							f.setXrefLabelNames(xref_entity.getXrefLabels());
						}
					}
				}
			}
		}

	}

	/**
	 * In each entity of an entity subclass hierarchy a 'type' field is added to enable filtering. This method adds this
	 * type as 'enum' field such that all subclasses are an enum option.
	 * 
	 * @param model
	 * @throws MolgenisModelException
	 */
	public static void addTypeFieldInSubclasses(Model model) throws MolgenisModelException
	{
		LOG.debug("add a 'type' field in subclasses to enable instanceof at database level...");
		for (Entity e : model.getEntities())
		{
			if (e.isRootAncestor())
			{
				Vector<Entity> subclasses = e.getAllDescendants();
				Vector<String> enumOptions = new Vector<String>();
				enumOptions.add(firstToUpper(e.getName()));
				for (Entity subclass : subclasses)
				{
					enumOptions.add(firstToUpper(subclass.getName()));
				}
				if (e.getField(Field.TYPE_FIELD) == null)
				{
					Field type_field = new Field(e, new EnumField(), Field.TYPE_FIELD, Field.TYPE_FIELD, true, false,
							true, null);
					type_field.setDescription("Subtypes have to be set to allow searching");
					// FIXME should be true, but breaks existing apps
					// type_field.setSystem(true);
					type_field.setHidden(true);
					e.addField(0, type_field);
				}
				e.getField(Field.TYPE_FIELD).setEnumOptions(enumOptions);
			}
			else
			{
				e.removeField(e.getField(Field.TYPE_FIELD));
			}
		}

	}

	/**
	 * Add link tables for many to many relationships
	 * <ul>
	 * <li>A link table entity will have the name of [from_entity]_[to_entity]
	 * <li>A link table has two xrefs to the from/to entity respectively
	 * <li>The column names are those of the respective fields
	 * <li>In case of a self reference, the second column name is '_self'
	 * </ul>
	 * 
	 * @param model
	 * @throws MolgenisModelException
	 */
	public static void createLinkTablesForMrefs(Model model, MolgenisOptions options) throws MolgenisModelException
	{
		LOG.debug("add linktable entities for mrefs...");
		// find the multi-ref fields
		for (Entity xref_entity_from : model.getEntities())
		{

			// iterate through all fields including those inherited from
			// interfaces
			for (Field xref_field_from : xref_entity_from.getImplementedFieldsOf(new MrefField()))
			{
				try
				{
					// retrieve the references to the entity+field
					Entity xref_entity_to = xref_field_from.getXrefEntity();
					if (xref_entity_to.isImported()) continue;

					Field xref_field_to = xref_field_from.getXrefField();

					// TODO: check whether this link is already present

					// create the new entity for the link, if explicitly
					// named
					String mref_name = xref_field_from.getMrefName(); // explicit

					// if mref_name longer than 30 throw error
					if (mref_name.length() > 30)
					{
						throw new MolgenisModelException("mref_name cannot be longer then 30 characters, found: "
								+ mref_name);
					}

					// check if the mref already exists
					Entity mrefEntity = null;
					try
					{
						mrefEntity = model.getEntity(mref_name);
					}
					catch (Exception e)
					{
					}

					// if mref entity doesn't exist: create
					if (mrefEntity == null)
					{
						mrefEntity = new Entity(mref_name, mref_name, model.getDatabase());
						mrefEntity.setNamespace(xref_entity_from.getNamespace());
						mrefEntity.setAssociation(true);
						mrefEntity.setDescription("Link table for many-to-many relationship '"
								+ xref_entity_from.getName() + "." + xref_field_from.getName() + "'.");
						mrefEntity.setSystem(true);

						// create id field to ensure ordering
						Field idField = new Field(mrefEntity, new IntField(), "autoid", "autoid", true, false, false,
								null);
						idField.setHidden(true);
						idField.setDescription("automatic id field to ensure ordering of mrefs");
						mrefEntity.addField(idField);
						mrefEntity.addKey(idField.getName(), "unique auto key to ensure ordering of mrefs");

						// create the fields for the linktable
						Field field;
						Vector<String> unique = new Vector<String>();

						field = new Field(mrefEntity, new XrefField(), xref_field_from.getMrefRemoteid(), null, false,
								false, false, null);
						field.setXRefVariables(xref_entity_to.getName(), xref_field_to.getName(),
								xref_field_from.getXrefLabelNames());
						if (xref_field_from.isXrefCascade()) field.setXrefCascade(true);
						mrefEntity.addField(field);

						unique.add(field.getName());

						// add all the key-fields of xref_entity_from
						for (Field key : xref_entity_from.getKeyFields(Entity.PRIMARY_KEY))
						{
							field = new Field(mrefEntity, new XrefField(), xref_field_from.getMrefLocalid(), null,
									false, false, false, null);

							// null xreflabel
							field.setXRefVariables(xref_entity_from.getName(), key.getName(), null);

							mrefEntity.addField(field);
							unique.add(field.getName());
						}

						// create the unique combination
						mrefEntity.addKey(unique, false, null);

					}
					// if mrefEntity does not exist, check xref_labels
					else
					{
						// field is xref_field, does it have label(s)?
						Field xrefField = mrefEntity.getAllField(xref_field_to.getName());

						// verify xref_label
						if (xrefField != null)
						{
							// logger.debug("adding xref_label "+xref_field_to.getXrefLabelNames()+"'back' for "+xrefField.getName());
							xrefField.setXrefLabelNames(xref_field_from.getXrefLabelNames());

						}
					}

					// set the linktable reference in the xref-field
					xref_field_from.setMrefName(mrefEntity.getName());
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}

	}

	/**
	 * Check if the view objects are an aggregate of known entities.
	 * 
	 * @param model
	 * @throws MolgenisModelException
	 */
	public static void validateViews(Model model) throws MolgenisModelException
	{
		// validate the views
		for (View view : model.getViews())
		{
			Vector<Entity> entities = new Vector<Entity>();
			Vector<Pair<Entity, Entity>> references = new Vector<Pair<Entity, Entity>>();

			// retrieve all the entities
			for (String viewentity : view.getEntities())
			{
				Entity entity = model.getEntity(viewentity);
				if (entity == null) throw new MolgenisModelException("Entity '" + viewentity + "' in view '"
						+ view.getName() + "' does not exist");

				entities.add(entity);
			}

			// validate that there are xref's pointing to the respective
			// entities
			for (Entity entity : entities)
			{
				for (Field field : entity.getFields())
				{
					if (!(field.getType() instanceof XrefField)) continue;

					// get the entity, which is referenced by the field
					Entity referenced = null;
					try
					{
						referenced = field.getXrefEntity();
					}
					catch (Exception e)
					{
						;
					}

					// check whether we're referencing one of the other entities
					// in the view
					for (Entity other : entities)
					{
						// exclude ourselves
						if (other.getName().equals(entity.getName())) continue;

						// check whether this is an entity we're referencing
						if (other.getName().equals(referenced.getName())) references.add(new Pair<Entity, Entity>(
								entity, other));
					}
				}
			}

			// if the sizes are not equal, then we could not link up all the
			// entities
			Vector<Entity> viewentities = new Vector<Entity>();
			for (Pair<Entity, Entity> p : references)
			{
				if (!viewentities.contains(p.getA())) viewentities.add(p.getA());
				if (!viewentities.contains(p.getB())) viewentities.add(p.getB());
			}

			// if (viewentities.size() != view.getEntities().size())
			// throw new DSLParseException("Cannot link up all the entities in "
			// + " view " + view.getName());
		}

	}

	/**
	 * Validate foreign key relationships: <li>
	 * <ul>
	 * Do the xref_field and xref_label refer to fields actually exist
	 * <ul>
	 * Is the entity refered to non-abstract
	 * <ul>
	 * Does the xref_field refer to a unique field (i.e. foreign key)</li>
	 * 
	 * @param model
	 * @throws MolgenisModelException
	 */
	public static void validateForeignKeys(Model model) throws MolgenisModelException
	{
		LOG.debug("validate xref_field and xref_label references...");

		// validate foreign key relations
		for (Entity entity : model.getEntities())
		{
			String entityname = entity.getName();

			for (Field field : entity.getFields())
			{
				String fieldname = field.getName();
				if (field.getType() instanceof XrefField || field.getType() instanceof MrefField)
				{

					String xref_entity_name = field.getXrefEntityName();
					String xref_field_name = field.getXrefFieldName();

					List<String> xref_label_names = field.getXrefLabelNames();

					// if no secondary key, use primary key
					if (xref_label_names.size() == 0)
					{
						xref_label_names.add(field.getXrefFieldName());
					}

					Entity xref_entity = model.getEntity(xref_entity_name);
					if (xref_entity == null) throw new MolgenisModelException("xref entity '" + xref_entity_name
							+ "' does not exist for field " + entityname + "." + fieldname);

					if (xref_field_name == null || xref_field_name.equals(""))
					{
						xref_field_name = xref_entity.getPrimaryKey().getName();
						field.setXrefField(xref_field_name);

						LOG.debug("automatically set " + entityname + "." + fieldname + " xref_field="
								+ xref_field_name);
					}

					if (!xref_entity.getName().equals(field.getXrefEntityName())) throw new MolgenisModelException(
							"xref entity '" + xref_entity_name + "' does not exist for field " + entityname + "."
									+ fieldname + " (note: entity names are case-sensitive)");

					if (xref_entity.isAbstract())
					{
						throw new MolgenisModelException("cannot refer to abstract xref entity '" + xref_entity_name
								+ "' from field " + entityname + "." + fieldname);
					}

					// if (entity.isAbstract()
					// && field.getType() instanceof MrefField) throw new
					// MolgenisModelException(
					// "interfaces cannot have mref therefore remove '"
					// + entityname + "." + fieldname + "'");

					Field xref_field = xref_entity.getField(xref_field_name, false, true, true);

					if (xref_field == null) throw new MolgenisModelException("xref field '" + xref_field_name
							+ "' does not exist for field " + entityname + "." + fieldname);

					// if (xref_field == null) xref_field =
					// xref_entity.getPrimaryKey();
					// throw new MolgenisModelException("xref field '" +
					// xref_field_name
					// + "' does not exist for field " + entityname + "." +
					// fieldname);

					for (String xref_label_name : xref_label_names)
					{
						Field xref_label = null;
						// test if label is defined as {entity}.{field}
						if (xref_label_name.contains("."))
						{
							xref_label = model.findField(xref_label_name);
						}
						// else assume {entity} == xref_entity
						else
						{
							xref_label = xref_entity.getAllField(xref_label_name);
						}
						// if null, check if a path to another xref_label:
						// 'fieldname_xreflabel'
						if (xref_label == null)
						{
							StringBuilder validFieldsBuilder = new StringBuilder();
							Map<String, List<Field>> candidates = field.allPossibleXrefLabels();

							if (candidates.size() == 0)
							{
								throw new MolgenisModelException(
										"xref label '"
												+ xref_label_name
												+ "' does not exist for field "
												+ entityname
												+ "."
												+ fieldname
												+ ". \nCouldn't find suitable secondary keys to use as xref_label. \nDid you set a unique=\"true\" or <unique fields=\" ...>?");
							}

							for (Entry<String, List<Field>> entry : candidates.entrySet())
							{
								String key = entry.getKey();
								if (xref_label_name.equals(key))
								{
									List<Field> value = entry.getValue();
									xref_label = value.get(value.size() - 1);
								}
								validFieldsBuilder.append(',').append(key);
							}

							// still null, must be error
							if (xref_label == null)
							{
								throw new MolgenisModelException("xref label '" + xref_label_name
										+ "' does not exist for field " + entityname + "." + fieldname
										+ ". Valid labels include " + validFieldsBuilder.toString());
							}

						}
						else
						{
							// validate the label

							if (!xref_label_name.equals(xref_field_name)
									&& !field.allPossibleXrefLabels().keySet().contains(xref_label_name))
							{
								String validLabels = StringUtils.join(field.allPossibleXrefLabels().keySet(), ',');
								throw new MolgenisModelException("xref label '" + xref_label_name + "' for "
										+ entityname + "." + fieldname
										+ " is not part a secondary key. Valid labels are " + validLabels
										+ "\nDid you set a unique=\"true\" or <unique fields=\" ...>?");
							}

						}

					}

					if (xref_field.getType() instanceof TextField) throw new MolgenisModelException("xref field '"
							+ xref_field_name + "' is of illegal type 'TEXT' for field " + entityname + "." + fieldname);

					boolean isunique = false;
					for (Unique unique : xref_entity.getAllKeys())
					{
						for (Field keyfield : unique.getFields())
						{
							if (keyfield.getName().equals(xref_field_name)) isunique = true;
						}
					}
					if (!isunique) throw new MolgenisModelException("xref pointer '" + xref_entity_name + "."
							+ xref_field_name + "' is a non-unique field for field " + entityname + "." + fieldname
							+ "\n" + xref_entity.toString());
				}
			}
		}
	}

	/**
	 * Validate the unique constraints
	 * <ul>
	 * <li>Do unique field names refer to existing fields?
	 * <li>Is there a unique column id + unique label?
	 * </ul>
	 * 
	 * @param model
	 * @throws MolgenisModelException
	 */
	public static void validateKeys(Model model) throws MolgenisModelException
	{
		LOG.debug("validate the fields used in 'unique' constraints...");
		// validate the keys
		for (Entity entity : model.getEntities())
		{
			String entityname = entity.getName();
			int autocount = 0;
			for (Field field : entity.getAllFields())
			{
				String fieldname = field.getName();
				if (field.isAuto() && field.getType() instanceof StringField)
				{
					autocount++;

					boolean iskey = false;

					for (Unique unique : entity.getAllKeys())
					{
						for (Field keyfield : unique.getFields())
						{
							if (keyfield.getName() == null) throw new MolgenisModelException("unique field '"
									+ fieldname + "' is not known in entity " + entityname);
							if (keyfield.getName().equals(field.getName())) iskey = true;
						}
					}

					if (!iskey) throw new MolgenisModelException(
							"there can be only one auto column and it must be the primary key for field '" + entityname
									+ "." + fieldname + "'");
				}

				if (field.getType() instanceof EnumField)
				{
					if (field.getDefaultValue() != null && !"".equals(field.getDefaultValue())) if (!field
							.getEnumOptions().contains(field.getDefaultValue()))
					{
						throw new MolgenisModelException("default value '" + field.getDefaultValue()
								+ "' is not in enum_options for field '" + entityname + "." + fieldname + "'");
					}
				}
			}

			if (autocount > 1) throw new MolgenisModelException(
					"there should be only one auto column and it must be the primary key for entity '" + entityname
							+ "'");

			// to strict, the unique field may be non-automatic
			if (!entity.isAbstract() && autocount < 1)
			{
				throw new MolgenisModelException(
						"there should be one auto column for each root entity and it must be the primary key for entity '"
								+ entityname + "'");
			}
		}

	}

	/**
	 * Validate extends and implements relationships:
	 * <ul>
	 * <li>Do superclasses actually exist
	 * <li>Do 'implements' refer to abstract superclasses (interfaces)
	 * <li>Do 'extends' refer to non-abstract superclasses
	 * <li>Copy primary key to subclass to form parent/child relationships
	 * </ul>
	 * 
	 * @param model
	 * @throws MolgenisModelException
	 */
	public static void validateExtendsAndImplements(Model model) throws MolgenisModelException
	{
		LOG.debug("validate 'extends' and 'implements' relationships...");
		// validate the extends and implements relations
		for (Entity entity : model.getEntities())
		{

			List<Entity> ifaces = entity.getAllImplements();
			for (Entity iface : ifaces)
			{
				if (!iface.isAbstract()) throw new MolgenisModelException(entity.getName() + " cannot implement "
						+ iface.getName() + " because it is not abstract");

				// copy primary key and xref_label from interface to subclass,
				// a primary key can have only one field.
				// usually it is a auto_number int
				// composite keys are ignored
				try
				{
					Field pkeyField = null;
					if (iface.getKeys().size() == 1)
					{
						pkeyField = iface.getKeyFields(Entity.PRIMARY_KEY).get(0);
						// if not already exists
						if (entity.getField(pkeyField.getName()) == null)
						{
							Field field = new Field(pkeyField);
							field.setEntity(entity);
							field.setAuto(pkeyField.isAuto());
							field.setNillable(pkeyField.isNillable());
							field.setReadonly(pkeyField.isReadOnly());
							field.setXRefVariables(iface.getName(), pkeyField.getName(), null);
							field.setHidden(true);

							LOG.debug("copy primary key " + field.getName() + " from interface " + iface.getName()
									+ " to " + entity.getName());
							entity.addField(field);

						}
					}

				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new MolgenisModelException(e.getMessage());
				}

			}

			Vector<String> parents = entity.getParents();
			if (parents.size() != 0)
			{
				Entity parent = model.getEntity(parents.get(0));
				if (parent == null) throw new MolgenisModelException("superclass '" + parents.get(0) + "' for '"
						+ entity.getName() + "' is missing");
				if (parent.isAbstract()) throw new MolgenisModelException(entity.getName() + " cannot extend "
						+ parents.get(0) + " because superclas " + parents.get(0) + " is abstract (use implements)");
				if (entity.isAbstract()) throw new MolgenisModelException(entity.getName() + " cannot extend "
						+ parents.get(0) + " because " + entity.getName() + " itself is abstract");

				if (parent.getKeys().size() == 0)
				{
					// log.out("panix");
					continue;

				}

				// copy primary key from superclass to subclass
				// try
				// {
				// Vector<String> keys = new Vector<String>();
				// for (Field key : parent.getKeyFields(Entity.PRIMARY_KEY))
				// {
				// if (entity.getField(key.getName()) == null)
				// {
				// Field field = new Field(key);
				// field.setEntity(entity);
				// field.setAuto(key.isAuto());
				// field.setNillable(key.isNillable());
				// field.setReadonly(key.isReadOnly());
				//
				// field.setSystem(true);
				// field.setXRefVariables(parent.getName(), key.getName(),
				// null);
				// field.setHidden(true);
				//
				// entity.addField(field);
				// logger.debug("copy primary key " + field.getName() +
				// " from superclass " + parent.getName()
				// + " to " + entity.getName());
				// keys.add(field.getName());
				// }
				// }
				// if (keys.size() > 0) entity.getKeys().add(0,
				// new Unique(entity, keys, false,
				// "unique reference to superclass"));
				// }
				// catch (Exception e)
				// {
				// throw new MolgenisModelException(e.getMessage());
				// }
			}
		}
	}

	/**
	 * Add interfaces as artificial entities to the model
	 * 
	 * @param model
	 * @throws MolgenisModelException
	 * @throws Exception
	 */
	public static void addInterfaces(Model model) throws MolgenisModelException
	{
		LOG.debug("add root entities for interfaces...");
		for (Entity entity : model.getEntities())
		{
			// Generate the interface if rootAncestor (so has subclasses) and
			// itself is not an interface...
			if (entity.isRootAncestor())
			{
				Entity rootAncestor = entity;
				if (!entity.isAbstract())
				{

					// generate a new interface
					rootAncestor = new Entity("_" + entity.getName() + "Interface", entity.getName(),
							model.getDatabase());
					rootAncestor
							.setDescription("Identity map table for "
									+ entity.getName()
									+ " and all its subclasses. "
									+ "For each row that is added to "
									+ entity.getName()
									+ " or one of its subclasses, first a row must be added to this table to get a valid primary key value.");
					// rootAncestor.setAbstract( true );

					// copy key fields to interface and unset auto key in child
					Vector<Field> keyfields = entity.getKey(0).getFields();
					Vector<String> keyfields_copy = new Vector<String>();
					for (Field f : keyfields)
					{
						Field key_field = new Field(rootAncestor, f.getType(), f.getName(), f.getName(), f.isAuto(),
								f.isNillable(), f.isReadOnly(), f.getDefaultValue());
						key_field.setDescription("Primary key field unique in " + entity.getName()
								+ " and its subclasses.");
						if (key_field.getType() instanceof StringField) key_field.setVarCharLength(key_field
								.getVarCharLength());
						rootAncestor.addField(key_field);
						keyfields_copy.add(key_field.getName());

						if (f.isAuto())
						{
							// unset auto key in original, but

							// SOLVED BY TRIGGERS Field autoField =
							// entity.getField(f.getName());
							// SOLVED BY TRIGGERS autoField.setAuto(false);

						}
					}
					rootAncestor.addKey(keyfields_copy, entity.getKey(0).isSubclass(), null);

					Vector<String> parents = new Vector<String>();
					parents.add(rootAncestor.getName());
					entity.setParents(parents);
				}

				// add the type enum to the root element
				Vector<Entity> subclasses = entity.getAllDescendants();
				Vector<String> enumOptions = new Vector<String>();
				enumOptions.add(entity.getName());
				for (Entity subclass : subclasses)
				{
					enumOptions.add(subclass.getName());
				}
				Field type_field = new Field(rootAncestor, new EnumField(), Field.TYPE_FIELD, Field.TYPE_FIELD, true,
						false, false, null);
				type_field.setDescription("Subtypes of " + entity.getName() + ". Have to be set to allow searching");
				type_field.setEnumOptions(enumOptions);
				type_field.setHidden(true);
				rootAncestor.addField(0, type_field);
			}
		}
	}

	public static void validateNamesAndReservedWords(Model model, MolgenisOptions options)
			throws MolgenisModelException
	{
		LOG.debug("check for JAVA and SQL reserved words...");
		Set<String> keywords = new HashSet<String>();
		keywords.addAll(ReservedKeywords.JAVA_KEYWORDS);
		keywords.addAll(ReservedKeywords.JAVASCRIPT_KEYWORDS);
		keywords.addAll(ReservedKeywords.ORACLE_KEYWORDS);
		keywords.addAll(ReservedKeywords.MYSQL_KEYWORDS);
		// keywords.addAll(ReservedKeywords.HSQL_KEYWORDS);

		if (model.getName().contains(" "))
		{
			throw new MolgenisModelException("model name '" + model.getName()
					+ "' illegal: it cannot contain spaces. Use 'label' if you want to show a name with spaces.");
		}

		// if(!containsOnlyLetters(model.getName()))
		// {
		// throw new MolgenisModelException("model name '" + model.getName()
		// + "' illegal: it can only contain letters, no numbers or dots");
		// }

		for (Module m : model.getModules())
		{
			if (m.getName().contains(" "))
			{
				throw new MolgenisModelException("module name '" + m.getName()
						+ "' illegal: it cannot contain spaces. Use 'label' if you want to show a name with spaces.");
			}
			// if(!containsOnlyLetters(m.getName()))
			// {
			// throw new MolgenisModelException("module name '" + m.getName()
			// + "' illegal: it can only contain letters, no numbers or dots");
			// }

		}

		for (Entity e : model.getEntities())
		{
			if (e.getName().contains(" "))
			{
				throw new MolgenisModelException("entity name '" + e.getName()
						+ "' cannot contain spaces. Use 'label' if you want to show a name with spaces.");
			}

			if (keywords.contains(e.getName().toUpperCase()) || keywords.contains(e.getName().toLowerCase()))
			{
				// e.setName(e.getName() + "_");
				// logger.warn("entity name '" + e.getName() + "' illegal:" +
				// e.getName() + " is a reserved word");
				throw new MolgenisModelException("entity name '" + e.getName() + "' illegal:" + e.getName()
						+ " is a reserved JAVA and/or SQL word and cannot be used for entity name");
			}
			for (Field f : e.getFields())
			{
				if (f.getName().contains(" "))
				{
					throw new MolgenisModelException("field name '" + e.getName() + "." + f.getName()
							+ "' cannot contain spaces. Use 'label' if you want to show a name with spaces.");
				}

				if (keywords.contains(f.getName().toUpperCase()) || keywords.contains(f.getName().toLowerCase()))
				{
					// f.setName(f.getName() + "_");
					// logger.warn("field name '" + f.getName() + "' illegal:" +
					// f.getName() + " is a reserved word");
					throw new MolgenisModelException("field name '" + e.getName() + "." + f.getName() + "' illegal: "
							+ f.getName() + " is a reserved JAVA and/or SQL word");
				}

				if (f.getType() instanceof XrefField || f.getType() instanceof MrefField)
				{
					String xref_entity = f.getXrefEntityName();
					if (xref_entity != null
							&& (keywords.contains(xref_entity.toUpperCase()) || keywords.contains(xref_entity
									.toLowerCase())))
					{
						// f.setXRefEntity(f.getXRefEntity() + "_");
						// logger.warn("field.xref-entity name '" + xref_entity
						// + "' illegal:" + xref_entity
						// + " is a reserved word");
						throw new MolgenisModelException("xref_entity reference from field '" + e.getName() + "."
								+ f.getName() + "' illegal: " + xref_entity + " is a reserved JAVA and/or SQL word");
					}

					if (f.getType() instanceof MrefField)
					{
						// default mref name is entityname+"_"+xreffieldname
						if (f.getMrefName() == null)
						{
							String mrefEntityName = f.getEntity().getName() + "_" + f.getName();

							// check if longer than 30 characters, then truncate
							if (mrefEntityName.length() > 30)
							{
								mrefEntityName = mrefEntityName.substring(0, 25)
										+ Integer.toString(mrefEntityName.hashCode()).substring(0, 5);
							}

							// paranoia check on uniqueness
							Entity mrefEntity = null;
							try
							{
								mrefEntity = model.getEntity(mrefEntityName);
							}
							catch (Exception exc)
							{
								throw new MolgenisModelException("mref name for " + f.getEntity().getName() + "."
										+ f.getName() + " not unique. Please use explicit mref_name=name setting");
							}

							if (mrefEntity != null)
							{
								mrefEntityName += "_mref";
								if (model.getEntity(mrefEntityName) != null)
								{
									mrefEntityName += "_" + Math.random();
								}
							}

							f.setMrefName(mrefEntityName);
						}
						if (f.getMrefLocalid() == null)
						{
							// default to entity name
							f.setMrefLocalid(f.getEntity().getName());
						}
						if (f.getMrefRemoteid() == null)
						{
							// default to xref entity name
							f.setMrefRemoteid(f.getName());
						}
					}
				}
			}
		}
	}

	/** test for case sensitivity */
	public static void correctXrefCaseSensitivity(Model model) throws MolgenisModelException
	{
		LOG.debug("correct case of names in xrefs...");
		for (Entity e : model.getEntities())
		{
			for (Field f : e.getFields())
			{
				// f.setName(f.getName().toLowerCase());

				if (f.getType() instanceof XrefField || f.getType() instanceof MrefField)
				{
					try
					{
						// correct for uppercase/lowercase typo's
						Entity xrefEntity = f.getXrefEntity();
						f.setXRefEntity(xrefEntity.getName());

						String xrefField = f.getXrefField().getName();

						List<String> xrefLabels = f.getXrefLabelsTemp();
						List<String> correctedXrefLabels = new ArrayList<String>();
						for (String xrefLabel : xrefLabels)
						{
							correctedXrefLabels.add(xrefEntity.getAllField(xrefLabel).getName());
						}
						f.setXRefVariables(xrefEntity.getName(), xrefField, correctedXrefLabels);
					}
					catch (Exception exception)
					{
						// exception.printStackTrace();
						// logger.error(exception);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param model
	 * @throws MolgenisModelException
	 */
	public static void copyDecoratorsToSubclass(Model model) throws MolgenisModelException
	{
		LOG.debug("copying decorators to subclasses...");
		for (Entity e : model.getEntities())
		{
			if (e.getDecorator() == null)
			{
				for (Entity superClass : e.getImplements())
				{
					if (superClass.getDecorator() != null)
					{
						e.setDecorator(superClass.getDecorator());
					}
				}
				for (Entity superClass : e.getAllAncestors())
				{
					if (superClass.getDecorator() != null)
					{
						e.setDecorator(superClass.getDecorator());
					}
				}
			}
		}
	}

	/**
	 * Copy fields to subclasses (redundantly) so this field can be part of an extra constraint. E.g. a superclass has
	 * non-unique field 'name'; in the subclass it is said to be unique and a copy is made to capture this constraint in
	 * the table for the subclass.
	 * 
	 * @param model
	 * @throws MolgenisModelException
	 */
	public static void copyFieldsToSubclassToEnforceConstraints(Model model) throws MolgenisModelException
	{
		LOG.debug("copy fields to subclass for constrain checking...");
		for (Entity e : model.getEntities())
		{
			// copy keyfields to subclasses to ensure that keys can be
			// enforced (if the key includes superclass fields).
			if (e.hasAncestor())
			{
				for (Unique aKey : e.getKeys())
				{
					for (Field f : aKey.getFields())
					{
						if (e.getField(f.getName()) == null)
						{
							// copy the field
							Field copy = new Field(f);
							copy.setEntity(e);
							copy.setAuto(f.isAuto());
							e.addField(copy);

							LOG.debug(aKey.toString() + " cannot be enforced on " + e.getName() + ", copying "
									+ f.getEntity().getName() + "." + f.getName() + " to subclass as " + copy.getName());
						}
					}
				}

			}
		}
	}

	private static String firstToUpper(String string)
	{
		if (string == null) return " NULL ";
		if (string.length() > 0) return string.substring(0, 1).toUpperCase() + string.substring(1);
		else return " ERROR[STRING EMPTY] ";
	}

}