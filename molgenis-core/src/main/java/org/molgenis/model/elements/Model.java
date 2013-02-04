/**
 * File: invengine_generate/meta/Model.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-12-06; 1.0.0; RA Scheltema; Creation.
 * </ul>
 */

package org.molgenis.model.elements;

// jdk
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.molgenis.model.MolgenisModelException;

/**
 * 
 */
public class Model implements Serializable
{
	private static final long serialVersionUID = 1L;

	// constructor
	/**
	 * 
	 */
	public Model(String name)
	{
		this.name = name;

		database = new DBSchema(name, null, this);
		userinterface = new UISchema(name, null);
		methods = new MethodSchema(name, null);

		database_description = "";
		userinterface_description = "";
		processing_description = "";
	}

	// general access methods
	/**
	 * 
	 */
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
		if (label == null) return this.getName();
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * 
	 */
	public DBSchema getDatabase()
	{
		return database;
	}

	/**
	 * 
	 */
	public UISchema getUserinterface()
	{
		return userinterface;
	}

	/**
	 * 
	 */
	public MethodSchema getMethodSchema()
	{
		return methods;
	}

	/**
	 * 
	 */
	public String getDBDescription()
	{
		return database_description;
	}

	/**
	 * 
	 */
	public void setDBDescription(String description)
	{
		this.database_description = description;
	}

	/**
	 * 
	 */
	public String getUIDescription()
	{
		return userinterface_description;
	}

	/**
	 * 
	 */
	public void setUIDescription(String description)
	{
		this.userinterface_description = description;
	}

	/**
	 * 
	 */
	public String getPRDescription()
	{
		return processing_description;
	}

	/**
	 * 
	 */
	public void setPRDescription(String description)
	{
		this.processing_description = description;
	}

	//
	/**
	 * 
	 */
	public Vector<View> getViews()
	{
		Vector<View> views = new Vector<View>();

		for (DBSchema element : database.getChildren())
		{
			if (element.getClass().equals(View.class))
			{
				views.add((View) element);
			}
		}
		return views;
	}

	public Vector<Entity> getEntities()
	{
		return getEntities(true);
	}

	/**
	 * Get entities that are NOT in modules (and are NOT mrefs). Used in
	 * generated file format documentation.
	 */
	public Vector<Entity> getRootEntities()
	{
		Vector<Entity> entities = new Vector<Entity>();

		List<String> entitiesInModules = new ArrayList<String>();
		for (Module m : this.getDatabase().getModules())
		{
			for (Entity e : m.getEntities())
			{
				entitiesInModules.add(e.getName());
			}
		}

		for (DBSchema element : database.getChildren())
		{
			if (element.getClass().equals(Entity.class) && !entitiesInModules.contains(((Entity) element).getName())
					&& !((Entity) element).isAssociation())
			{
				entities.add((Entity) element);
			}
		}

		return entities;
	}

	/**
	 * 
	 */
	public Vector<Entity> getEntities(boolean includeSystemTable, boolean includeNonConcretes)
	{
		Vector<Entity> entities = new Vector<Entity>();

		for (DBSchema element : database.getChildren())
		{
			if (element.getClass().equals(Entity.class))
			{
				if (includeSystemTable || !((Entity) element).isSystem())
				{
					if (includeNonConcretes || !((Entity) element).isAbstract())
					{
						entities.add((Entity) element);
					}
				}
			}
		}
		return entities;
	}

	/**
	 * 
	 */
	public Vector<Entity> getEntities(boolean includeSystemTable)
	{
		return getEntities(includeSystemTable, true);
	}

	public List<Module> getModules()
	{
		return this.getDatabase().getModules();
	}

	public Vector<Matrix> getMatrices()
	{
		Vector<Matrix> matrices = new Vector<Matrix>();

		for (DBSchema element : database.getChildren())
		{
			if (element.getClass().equals(Matrix.class))
			{
				matrices.add((Matrix) element);
			}
		}
		return matrices;
	}

	public Vector<Entity> getConcreteEntities()
	{
		Vector<Entity> concrete_entities = new Vector<Entity>();
		for (Entity e : getEntities())
		{
			if (!e.isAbstract())
			{
				concrete_entities.add(e);
			}
		}
		return concrete_entities;
	}

	/**
	 * 
	 */
	public Vector<Method> getMethods()
	{
		Vector<Method> themethods = new Vector<Method>();

		for (MethodSchema element : methods.getChildren())
			if (element.getClass().equals(Method.class))
			{
				themethods.add((Method) element);
			}
		return themethods;
	}

	/**
	 * @throws MolgenisModelException
	 * 
	 */
	public Entity getEntity(String name)
	{
		for (DBSchema element : database.getAllChildren())
		{
			if (element.getClass().equals(Entity.class) && element.getName().equalsIgnoreCase(name))
			{
				return (Entity) element;
			}
		}
		// throw error if cannot find it (don't return null).
		// throw new
		// MolgenisLanguageException("couldn't find entity '"+name+"'");
		// throw new RuntimeException();
		// System.exit(1);
		return null;
	}

	/**
	 * 
	 */
	public Record getRecord(String name)
	{
		for (DBSchema element : database.getChildren())
		{
			if (!element.getName().equals(name)) continue;

			for (Class<?> cl : element.getClass().getInterfaces())
			{
				if (cl.equals(Record.class)) return (Record) element;
			}
		}
		// throw error if cannot find it (don't return null).
		// throw new RuntimeException();
		// System.exit(1);
		return null;
	}

	/**
	 * 
	 */
	// public static void createSystemTables(Model model)
	// {
	// Field field;
	// Entity entity;
	//
	// try
	// {
	// FileMetaInfo fileinfo = model.getFileInfo();
	// UserMetaInfo userinfo = model.getUserInfo();
	// RoleMetaInfo roleinfo = model.getRoleInfo();
	// EntityMetaInfo entityinfo = model.getEntityInfo();
	// ScreenMetaInfo screeninfo = model.getScreenInfo();
	//
	// // files
	// entity = new Entity(fileinfo.getEntity(), "", model.getDatabase(), true);
	//
	// field = new Field(entity, Field.Type.INT, fileinfo.getColumnNameID(), "",
	// true, false, false, "");
	// entity.addField(field);
	// entity.addKey(field,null);
	//
	// // Vector<Field> constraint_name = new Vector<Field>();
	//
	// field = new Field(entity, Field.Type.STRING,
	// fileinfo.getColumnNameFilename(), "", false, true, false, "");
	// field.setVarCharLength(1024);
	// entity.addField(field);
	// // constraint_name.add(field);
	//
	// field = new Field(entity, Field.Type.STRING,
	// fileinfo.getColumnNameEntityOwner(), "", false, true, false,
	// "");
	// field.setVarCharLength(1024);
	// entity.addField(field);
	// // constraint_name.add(field);
	//
	// // entity.addKey(constraint_name);
	//
	// field = new Field(entity, Field.Type.STRING,
	// fileinfo.getColumnNameLocalFileName(), "", false, true, false,
	// "");
	// field.setVarCharLength(1024);
	// entity.addField(field);
	//
	// field = new Field(entity, Field.Type.STRING,
	// fileinfo.getColumnNameMime(), "", false, true, false, "");
	// field.setVarCharLength(255);
	// entity.addField(field);
	//
	// // users
	// entity = new Entity(userinfo.getEntity(), "", model.getDatabase(), true);
	//
	// field = new Field(entity, Field.Type.INT, userinfo.getColumnNameID(), "",
	// true, false, false, "");
	// entity.addField(field);
	// entity.addKey(field);
	//
	// field = new Field(entity, Field.Type.STRING,
	// userinfo.getColumnNameInitials(), "", false, true, false, "");
	// field.setVarCharLength(32);
	// entity.addField(field);
	//
	// field = new Field(entity, Field.Type.STRING,
	// userinfo.getColumnNameLastName(), "", false, true, false, "");
	// field.setVarCharLength(32);
	// entity.addField(field);
	//
	// // name can only be unique if not null!
	// field = new Field(entity, Field.Type.STRING,
	// userinfo.getColumnNameUsername(), "", false, false, false, "");
	// field.setVarCharLength(32);
	// entity.addField(field);
	// entity.addKey(field); // make the username unique
	//
	// field = new Field(entity, Field.Type.STRING,
	// userinfo.getColumnNamePassword(), "", false, true, false, "");
	// field.setVarCharLength(32);
	// entity.addField(field);
	//
	// field = new Field(entity, Field.Type.XREF_MULTIPLE,
	// userinfo.getColumnNameRoles(), "", false, true, false,
	// "");
	// field.setXRefVariables(roleinfo.getEntity(), roleinfo.getColumnNameID(),
	// roleinfo.getColumnNameRoleName());
	// // field.setXRefLinkTable("");
	// entity.addField(field);
	//
	// // roles
	// entity = new Entity(roleinfo.getEntity(), "", model.getDatabase(), true);
	//
	// field = new Field(entity, Field.Type.INT, roleinfo.getColumnNameID(), "",
	// true, false, false, "");
	// entity.addField(field);
	// entity.addKey(field);
	//
	// // name can only be unique if not null!
	// field = new Field(entity, Field.Type.STRING,
	// roleinfo.getColumnNameRoleName(), "", false, false, false, "");
	// field.setVarCharLength(255);
	// entity.addField(field);
	// entity.addKey(field);
	//
	// // entities
	// entity = new Entity(entityinfo.getEntity(), "", model.getDatabase(),
	// true);
	//
	// field = new Field(entity, Field.Type.INT, entityinfo.getColumnNameID(),
	// "", true, false, false, "");
	// entity.addField(field);
	// entity.addKey(field);
	//
	// // name can only be unique if not null!
	// field = new Field(entity, Field.Type.STRING,
	// entityinfo.getColumnNameName(), "", false, false, false, "");
	// field.setVarCharLength(255);
	// entity.addField(field);
	// entity.addKey(field);
	//
	// field = new Field(entity, Field.Type.XREF_MULTIPLE,
	// entityinfo.getColumnNameRoles(), "", false, true,
	// false, "");
	// field.setXRefVariables(roleinfo.getEntity(), roleinfo.getColumnNameID(),
	// roleinfo.getColumnNameRoleName());
	// // field.setXRefLinkTable("");
	// entity.addField(field);
	//
	// // screens
	// entity = new Entity(screeninfo.getEntity(), "", model.getDatabase(),
	// true);
	//
	// field = new Field(entity, Field.Type.INT, screeninfo.getColumnNameID(),
	// "", true, false, false, "");
	// entity.addField(field);
	// entity.addKey(field);
	//
	// field = new Field(entity, Field.Type.STRING,
	// screeninfo.getColumnNameName(), "", false, true, false, "");
	// field.setVarCharLength(255);
	// entity.addField(field);
	// // entity.addKey(field);
	//
	// field = new Field(entity, Field.Type.XREF_MULTIPLE,
	// screeninfo.getColumnNameRoles(), "", false, true,
	// false, "");
	// field.setXRefVariables(roleinfo.getEntity(), roleinfo.getColumnNameID(),
	// roleinfo.getColumnNameRoleName());
	// // field.setXRefLinkTable("");
	// entity.addField(field);
	// }
	// catch (Exception e)
	// {
	// }
	// }
	@Override
	public String toString()
	{
		StringBuffer result = new StringBuffer();
		for (Entity e : getEntities())
		{
			result.append("ENTITY: " + e.toString() + "\n");
			// result.append("ENTITY:"+e.getName()+"\n");
		}

		for (Matrix m : getMatrices())
		{
			result.append("MATRIX: " + m.toString() + "\n");
			// result.append("ENTITY:"+e.getName()+"\n");
		}

		for (UISchema u : getUserinterface().getAllChildren())
		{
			result.append(u.toString() + "\n");
		}

		return result.toString();
	}

	// member variables
	/** The name of the model */
	private String name;

	/** The pretty label of the model */
	private String label;

	/** The meta model for the database */
	private DBSchema database;

	/** The meta model for the userinterface */
	private UISchema userinterface;

	/** The meta model for methods */
	private MethodSchema methods;

	/** */
	private String database_description;

	/** */
	private String userinterface_description;

	/** */
	private String processing_description;

	public Field findField(String f) throws MolgenisModelException
	{
		// pattern {entity}.{field}
		// or pattern {null}.{field} but only if field name is unique
		StringTokenizer tok = new StringTokenizer(f, ".");
		String entity = null;
		String field = null;
		if (tok.countTokens() == 1)
		{
			field = tok.nextToken();
		}
		else if (tok.countTokens() == 2)
		{
			entity = tok.nextToken();
			field = tok.nextToken();
		}
		else
		{
			throw new MolgenisModelException("field with name '" + f + " is unknown" + tok.countTokens());
		}

		// get entity
		Field result = null;
		if (entity != null)
		{
			Entity em;
			try
			{
				// todo: make case insensitive?
				em = this.getEntity(entity);
			}
			catch (Exception e)
			{
				throw new MolgenisModelException("field with name '" + f + " is unknown: " + e.getMessage());
			}

			// get field
			result = em.getAllField(field);
		}
		else
		{
			int count = 0;
			for (Entity em : getEntities())
			{
				for (Field fm : em.getAllFields())
				{
					if (fm.getName().equalsIgnoreCase(field))
					{
						result = fm;
						count++;
						if (count > 1) throw new MolgenisModelException("field with name '" + f
								+ " is not unique, please provide entity also in format {entity}.{field}");
					}
				}
			}
		}
		if (result != null)
		{
			return result;
		}
		else
		{
			throw new MolgenisModelException("field with name '" + f + "' is unknown: ");
		}
	}

	public int getNumberOfReferencesTo(Entity e) throws MolgenisModelException
	{
		int count = 0;

		for (Entity entity : this.getEntities())
		{
			for (Field field : entity.getImplementedFields())
			{
				if (field.isXRef() || field.isMRef())
				{
					String xrefEntity = field.getXrefEntityName();
					if (xrefEntity != null && xrefEntity.equals(e.getName())) count++;
				}
			}
		}
		return count;
	}
}
