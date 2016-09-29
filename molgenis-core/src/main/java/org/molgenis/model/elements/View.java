// File: invengine_generate/meta/Entity.java <br>
// Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
// Changelog:
// 
// 2006-09-29; 1.0.0; RA Scheltema;
//	Creation.
//

package org.molgenis.model.elements;

// imports

import org.molgenis.fieldtypes.XrefField;
import org.molgenis.model.MolgenisModelException;

import java.util.List;
import java.util.Vector;

/**
 * This class describes an updatable view, which shows two or more entities.
 * This can only be a liniair path in the entity graph otherwise updates are
 * undefined.
 *
 * @author RA Scheltema
 * @author MA Swertz
 * @version 1.0.0
 */

public class View extends DBSchema implements Record
{
	// constructor(s)

	/**
	 *
	 */
	public View(String name, String label, DBSchema parent)
	{
		super(name, parent, parent.getModel());

		//
		this.label = label;
	}

	// @Override
	@Override
	public String getLabel()
	{
		return label;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public List<Field> getAllFields() throws MolgenisModelException
	{
		return getFields();
	}

	// @Override
	@Override
	public List<Field> getFields() throws MolgenisModelException
	{
		// retrieve the root
		DBSchema database = getRoot();

		// retrieve all the fields
		Vector<Field> fields = new Vector<Field>();
		for (String viewentity : entities)
		{
			Entity entity = (Entity) database.get(viewentity);

			for (Field field : entity.getAllFields())
			{
				Field f = new Field(field);

				// for now we mis-use the user-data pointer to store the
				// original name
				f.setUserData(field.getName());

				// generate the new name, which is a concatenation of the
				// entity-name with field-name
				f.setName(entity.getName() + "_" + field.getName());
				f.setLabel(entity.getName() + "::" + field.getName());

				fields.add(f);
			}
		}

		return fields;
	}

	// @Override
	@Override
	public boolean hasXRefs()
	{
		// retrieve the root
		DBSchema database = getRoot();

		// retrieve all the fields
		for (String viewentity : entities)
		{
			if (((Entity) database.get(viewentity)).hasXRefs()) return true;
		}

		return false;
	}

	// @Override
	@Override
	public Vector<String> getParents()
	{
		return new Vector<String>();
	}

	// access

	/**
	 *
	 */
	public void addEntity(String entity)
	{
		if (!entities.contains(entity)) entities.add(entity);
	}

	/**
	 *
	 */
	public List<String> getEntities()
	{
		return entities;
	}

	/**
	 *
	 */
	public int getNrEntities()
	{
		return entities.size();
	}

	/**
	 * @throws MolgenisModelException
	 */
	public List<Field> getXRefsFor(Entity e, List<Entity> entities) throws MolgenisModelException
	{
		Vector<Field> xrefs = new Vector<Field>();

		for (Entity entity : entities)
		{
			// check whether e has a reference to this entity
			for (Field field : e.getAllFields())
			{
				if (!(field.getType() instanceof XrefField)) continue;

				try
				{
					if (field.getXrefEntity().getName().equals(entity.getName())) xrefs.add(field);
				}
				catch (Exception ex)
				{
				}
			}

			// check whether this entity has a reference to e
			for (Field field : entity.getAllFields())
			{
				if (!(field.getType() instanceof XrefField)) continue;

				try
				{
					if (field.getXrefEntity().getName().equals(e.getName())) xrefs.add(field);
				}
				catch (Exception ex)
				{
				}
			}
		}

		return xrefs;
	}

	// data
	/**  */
	String label = "";
	/** */
	String description = "";
	/**  */
	public Vector<String> entities = new Vector<String>();

	/**  */
	private static final long serialVersionUID = 0;

}
